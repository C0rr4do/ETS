package com.ets.app.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
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
    private val dateRegex = Regex("""Vertretungen\s+(\d{2})\.(\d{2})\..+""")

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

                // Parse substitution-plan of file
                var parsedPlan = parsePlan(planName, file)

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
        if (file == null) return Timestamps.UNKNOWN_TIMESTAMP
        else {
            // load pdf document from file and extract text
            val doc = PDDocument.load(file, getPassword())

            val stripper = PDFTextStripper()
            stripper.startPage = 1
            stripper.endPage = 1

            val lines = stripper.getText(doc).split('\n')
            doc.close()
            var index = 0

            // iterate through lines
            while (index < lines.size) {
                // find line which matches the pattern
                if (dateRegex.matches(lines[index])) {
                    // parse date
                    return parseDate(lines[index])
                }

                // goto next line
                index++
            }

            // return unknown timestamp if no line was found
            return Timestamps.UNKNOWN_TIMESTAMP
        }
    }

    // parses the date in the specified line
    private fun parseDate(line: String): Long {
        // check if line matches
        val match = dateRegex.find(line)

        return if (match != null) {
            // extract day and month
            val day = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            // get the current year
            val year = DateTime.now().year

            // return milliseconds of the parsed time
            DateTime(year, month, day, 0, 0, 0).millis
        } else Timestamps.UNKNOWN_TIMESTAMP
    }

    // parses a substitution plan from the specified file
    private fun parsePlan(planName: String, file: File?): SubstitutionPlan? {
        // return null if no file was given
        return if (file == null) {
            null
        } else {
            // load pdf document and extract all text
            val doc = PDDocument.load(file, getPassword())

            val stripper = PDFTextStripper()
            stripper.startPage = 1
            stripper.endPage = doc.numberOfPages

            val text: String = stripper.getText(doc)
            doc.close()

            // split text into lines and create variables needed for parsing
            val lines = text.split("\n")
            val substitutions = mutableListOf<Substitution>()
            var date = Timestamps.UNKNOWN_TIMESTAMP

            var index = 0
            var id = 0

            // iterate through lines
            while (index < lines.size) {
                // try to parse courses from the line
                val courses = parseCourse(lines, index)
                // select the current line
                var line = lines[index]
                try {
                    // courses where found on this line
                    if (courses.result != null) {
                        // extract lessons, subjects, rooms and additional substitution information
                        var lessons = parseLessons(line)
                        var subject = parseSubject(line, lessons.endIndex)
                        var room = parseRoom(line, subject.endIndex)
                        var subSubject = parseSubject(line, room.endIndex)
                        var subRoom = parseRoom(line, subSubject.endIndex)
                        var type = parseType(line, subRoom.endIndex)
                        var info = line.substring(type.endIndex).trim()

                        // set the right substitution subject if no substitution subject was parsed
                        if (subSubject.result == Subject.UNKNOWN) {
                            if (type.result == "Vertretung" || type.result == "Fachbetreuung") {
                                subSubject.result = subject.result
                            } else if (type.result == "frei") {
                                subSubject.result = Subject.CANCELED
                            }
                        }

                        // add new substitution object to collection
                        substitutions.add(
                            Substitution(
                                id++,
                                courses.result,
                                lessons.result,
                                subject.result,
                                room.result,
                                subSubject.result,
                                subRoom.result,
                                type.result,
                                info
                            )
                        )

                        // increase the line index by number of lines processed
                        index += courses.lines
                    } else if (dateRegex.containsMatchIn(line)) {
                        // parse substitution plan date if was found on line
                        date = parseDate(line)
                    }
                } catch (e: Exception) {
                    // display exceptions on ui
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

                // increase the line index by 1
                index++
            }

            // return substitution plan
            SubstitutionPlan(planName.toLong(), date, "", arrayOf(), substitutions)
        }
    }

    // extracts courses from current and following lines
    private fun parseCourse(lines: List<String>, index: Int): CourseParserData {
        // create variables
        var courseString = ""
        var curr = index;

        do {
            // search for courses on current line
            var line = lines[curr]
            var match = courseRegex.findAll(line)

            var lastMatch = true;
            // iterate through found matches and store them
            match.forEach {
                courseString += it.value

                // determine whether further matches are to be expected or not
                lastMatch = it.groupValues[1] == ""
            }

            // increase line index if further matches are to be expected
            if (!lastMatch) {
                curr++
            }

        } while (!lastMatch && curr < lines.size) // stop parsing if all lines were processed or no further matches are to be expected

        // if courses were found
        if (courseString.isNotEmpty()) {
            // split course string
            var courseStringParts = courseString.split(',').map { it.trim() }

            // parse course strings
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

    // parses lessons from the specified line
    private fun parseLessons(line: String): LessonParserData {
        // search for lessons in given line
        val match = lessonRegex.find(line)

        // parse courses if found
        if (match != null) {
            val endIndex = match.range.last + 1

            val str = match.groupValues[0].replace(" ", "")

            val parts = str.split("-")
            val start = parts[0].toInt()

            return if (parts.size == 2) {
                val end = parts[1].toInt()
                LessonParserData(IntRange(start, end), endIndex)
            } else {
                LessonParserData(IntRange(start, start), endIndex)
            }
        }

        return LessonParserData(IntRange(0, 0), 0)
    }

    // parse subject from specified line at start index
    private fun parseSubject(line: String, index: Int): SubjectParserData {
        // search for subject at given position
        val match = subjectRegex.find(line, index)

        // return result
        return if (match != null) SubjectParserData(
            getSubject(match.value.trim()),
            match.range.last + 1
        )
        else SubjectParserData(Subject.UNKNOWN, index)
    }

    // parse room from specified line at start index
    private fun parseRoom(line: String, index: Int): ParserData {
        // search for room at given position
        val match = roomRegex.find(line, index)

        // return result
        return if (match != null) ParserData(match.value.trim(), match.range.last + 1)
        else ParserData("", index)
    }

    // parse substitution type from specified line at start index
    private fun parseType(line: String, index: Int): ParserData {
        // search for substitution type at given position
        val match = typeRegex.find(line, index)

        // return result
        return if (match != null) ParserData(match.value.trim(), match.range.last + 1)
        else ParserData("", index)
    }

    // get subject
    private fun getSubject(id: String): Subject {
        // find subject in class
        val subject = Subject.values().firstOrNull { it.id == id }
        // return subject if exists and return it otherwise log a message and throw an exception
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

    data class LessonParserData(var result: IntRange, val endIndex: Int)

    data class SubjectParserData(var result: Subject, val endIndex: Int)

    data class ParserData(var result: String, val endIndex: Int)
}