package com.ets.app.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.ets.app.model.Course
import com.ets.app.model.Subject
import com.ets.app.model.Substitution
import com.ets.app.model.SubstitutionPlan
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParsingService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileService: FileService
) {
    private val parsedDates = HashMap<String, Long>()
    private val parsedPlans = HashMap<String, SubstitutionPlan>()

    private val _runningJobIds = MutableLiveData(mutableListOf<Int>())
    val parsing = Transformations.map(_runningJobIds) { it.size > 0 }

    // regular expressions
    private val courseRegex = Regex("""\d{3}[a-z](\,)?|[QE]\d\/\d\s+\w+\d+""")
    private val lessonRegex = Regex("""\s\d\s(\-\s\d)?""")
    private val roomRegex = Regex("""[A-Z0-9]+\s""")
    private val subjectRegex = Regex("""[A-Z\u00C4\u00D6\u00DC]+\s""")
    private val typeRegex =
        Regex("""Raumänderung|Vertretung|frei|Trotz Absenz|Fachbetreuung|Sondereins\.|Verlegung|Unterricht geändert""")

    @Volatile
    private var parsingJobCount = 0

    suspend fun getParsedDate(planName: String): Long? {
        // If date of this substitution-plan was parsed and cached already
        if (parsedDates.containsKey(planName)) {
            // Return cached date instead of re-parsing it
            return parsedDates[planName]!!
        } else {
            // Get unique jobId
            val jobId = parsingJobCount++

            // Indicate that this parsing job is started now
            markAsRunning(jobId)

            return withContext(Dispatchers.Default) {
                // Get file that corresponds to 'planName'
                val file = fileService.getFileByName(planName)

                // Parse substitution-plan-date of file
                val parsedDate = parseDate(file)

                // If date was parsed successfully, store result in cache
                parsedDates[planName] = parsedDate

                // Indicate that this job is finished now
                markAsFinished(jobId)

                return@withContext parsedDate
            }
        }
    }

    suspend fun getParsedPlan(planName: String): SubstitutionPlan? {
        // If this substitution-plan was parsed and cached already
        if (parsedPlans.containsKey(planName)) {
            // Return cached substitution-plan instead of re-parsing it
            return parsedPlans[planName]!!
        } else {
            // Get unique jobId
            val jobId = parsingJobCount++

            // Indicate that this parsing job is started now
            markAsRunning(jobId)

            return withContext(Dispatchers.Default) {
                // Get file that corresponds to 'planName'
                val file = fileService.getFileByName(planName)

                var parsedPlan: SubstitutionPlan? = null
                try {
                    // Parse substitution-plan of file
                    parsedPlan = parsePlan(planName, file)
                } catch (e: Exception) {
                    with(Handler(Looper.getMainLooper())) {
                        post {
                            Toast.makeText(
                                context,
                                "Error while parsing substitution-plan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                // If substitution-plan was parsed successfully, store result in cache
                parsedPlan?.let {
                    parsedPlans[planName] = it
                }

                markAsFinished(jobId)

                return@withContext parsedPlan
            }
        }
    }

    private fun markAsRunning(jobId: Int) {
        _runningJobIds.postValue(_runningJobIds.value!!.apply { add(jobId) })
    }

    private fun markAsFinished(jobId: Int) {
        _runningJobIds.postValue(_runningJobIds.value!!.apply { remove(jobId) })
    }

    private fun parseDate(file: File?): Long {
        // TODO Implement functionality
        if (file == null) {
            return Timestamps.UNKNOWN_TIMESTAMP
        } else {
            return Timestamps.UNKNOWN_TIMESTAMP
        }
    }

    private fun parsePlan(planName: String, file: File?): SubstitutionPlan? {
        return if (file == null) {
            null
        } else {
            val data = file.readBytes()
            val doc = PDDocument.load(data, getPassword())

            val stripper = PDFTextStripper()
            stripper.startPage = 1
            stripper.endPage = doc.numberOfPages

            val text: String = stripper.getText(doc)
            doc.close()

            val lines = text.split("\n")
            val substitutions = mutableListOf<Substitution>()

            var index = 0
            var id = 0

            while (index < lines.size) {
                val courses = parseCourse(lines, index)

                if (courses.result != null) {
                    var line = lines[index]
                    val lessons = parseLessons(line)
                    val subject = parseSubject(line, lessons.endIndex)
                    val room = parseRoom(line, subject.endIndex)
                    val subSubject = parseSubject(line, room.endIndex)
                    val subRoom = parseRoom(line, subSubject.endIndex)
                    val type = parseType(line, subRoom.endIndex)
                    val info = line.substring(type.endIndex).trim()

                    // println("${course.result}\t${lessons.result}\t${subject.result}\t${room.result}\t${subSubject.result}\t${subRoom.result}\t${type.result}\t${info}")
                    substitutions.add(Substitution(id++, courses.result, lessons.result, subject.result, room.result, subSubject.result, subRoom.result, type.result, info))
                    index += courses.lines + 1
                } else {
                    index++
                }
            }

            SubstitutionPlan(planName.toLong(), parseDate(file), "", arrayOf(), substitutions)
        }
    }

    private fun parseCourse(lines: List<String>, index: Int): CourseParserData {
        var courseString = ""
        var curr = index;

        do {
            var line = lines[curr]
            var match = courseRegex.findAll(line)

            var lastMatch = true;
            match.forEach {
                courseString += it.value

                lastMatch = it.groupValues[1] == ""
            }

            if (!lastMatch) {
                curr++
            }
        } while (!lastMatch && curr < lines.size)

        if (courseString.isNotEmpty()) {
            var courseStringParts = courseString.split(',').map { it.trim() }

            var courses = courseStringParts.map {
                if (it.startsWith("Q") || it.startsWith("E")) {
                    val parts = it.split(' ')

                    Course(
                        when (parts[0]) {
                            "E1/2" -> 11;
                            "Q1/2" -> 12;
                            "Q3/4" -> 13;
                            else -> -1
                        }, parts[1]
                    )
                } else {
                    val classId = it.substring(0..1).toInt()
                    val courseId = it[3].toString()

                    Course(classId, courseId)
                }
            }
            return CourseParserData(courses, curr - index)
        }

        return CourseParserData(null, index)
    }

    private fun parseLessons(line: String): LessonParserData {
        val match = lessonRegex.find(line)

        if (match != null) {
            val endIndex = match.range.last + 1

            val str = match.groupValues[0].replace(" ", "")

            val parts = str.split("-")
            val start = parts[0].toInt()
            if (parts.size == 2) {
                val end = parts[1].toInt()
                return LessonParserData(IntRange(start, end), endIndex)
            } else {
                return LessonParserData(IntRange(start, start), endIndex)
            }
        }

        return LessonParserData(IntRange(0, 0), 0)
    }

    private fun parseSubject(line: String, index: Int): SubjectParserData {
        val match = subjectRegex.find(line, index)

        return if (match != null) SubjectParserData(getSubject(match.value.trim()), match.range.last + 1)
        else SubjectParserData(Subject.UNKNOWN, index)
    }

    private fun parseRoom(line: String, index: Int): ParserData {
        val match = roomRegex.find(line, index)

        return if (match != null) ParserData(match.value.trim(), match.range.last + 1)
        else ParserData("", index)
    }

    private fun parseType(line: String, index: Int): ParserData {
        val match = typeRegex.find(line, index)

        return if (match != null) ParserData(match.value.trim(), match.range.last + 1)
        else ParserData("", index)
    }

    private fun getSubject(id: String): Subject {
        val subject = Subject.values().firstOrNull { it.id == id }
        if (subject == null) {
            Timber.e("Subject not found: $id")
            throw NoSuchElementException()
        }
        return subject
    }

    private fun getPassword(): String {
        // TODO Do not hardcode this
        return "pennenspatz"
    }

    data class CourseParserData(val result: List<Course>?, val lines: Int)

    data class LessonParserData(val result: IntRange, val endIndex: Int)

    data class SubjectParserData(val result: Subject, val endIndex: Int)

    data class ParserData(val result: String, val endIndex: Int)
}