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

    @Volatile private var parsingJobCount = 0

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
                    with (Handler(Looper.getMainLooper())) {
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

            while (index < lines.size) {
                while (index < lines.size && !lines[index].startsWith("Klasse")) {
                    index++
                }

                index++
                while (index < lines.size && !lines[index].startsWith("Edertalschule")) {
                    var line = lines[index]
                    if (lines.size > index + 1) {
                        var nextLine = lines[index + 1]
                        if (nextLine.length <= 5) {
                            val startIndex = line.indexOf(',')
                            val first = line.substring(0..startIndex)
                            val last = line.substring(startIndex + 1)

                            line = first
                            index++
                            while (index < lines.size && nextLine.length <= 5) {
                                line += nextLine
                                index++

                                if (index < lines.size) {
                                    nextLine = lines[index]
                                }
                            }

                            line += last
                        } else {
                            index++
                        }
                    }

                    if (line.isNotEmpty()) {
                        substitutions.add(parseSubstitutionLine(line))
                    }
                }
            }

            SubstitutionPlan(planName.toLong(), parseDate(file), "", arrayOf(), substitutions)
        }
    }

    private fun parseSubstitutionLine(line: String): Substitution {

        // course
        val courses: Array<Course>
        val courseRegex = Regex("""((\d{3}\w,\s*)*(\d{3}\w))|[EQ]\d/\d\s+\w+\d+""")
        val courseString = courseRegex.find(line)?.value

        if (courseString != null) {
            val courseStringParts = courseString.split(',')
            if (!(line[0] == 'E' || line[0] == 'Q')) {
                courses = Array(courseStringParts.size) { i ->
                    Course(
                        courseStringParts[i].trim(),
                        null,
                        courseStringParts[i].trim().removeRange(2, 3).trimStart('0')
                    )
                }
            } else {
                courses = Array(courseStringParts.size) {
                    Course("null", null, "null")
                }

                courseStringParts.forEachIndexed { index, element ->
                    val courseParts = element.split(' ')
                    courses[index] = Course(
                        courseParts[0].trim(),
                        courseParts[1].trim(),
                        "${courseParts[0].trim()}(${courseParts[1].trim()})"
                    )
                }
            }
        } else {
            courses = Array(0) { Course("null", null, "null") }
        }

        var index = 0
        val parts = line.removeRange(0, courseString?.length ?: 0).trimStart().split(' ')

        // lessons
        val lessons: Array<Int>
        if (parts[index + 1] == "-") {
            lessons = arrayOf(parts[index].toInt(), parts[index + 2].toInt())
            index += 3
        } else {
            lessons = arrayOf(parts[index].toInt())
            index++
        }

        // subject
        val subject = getSubject(parts[index])
        index++

        // roomId
        val roomId = parts[index]
        index++

        var subSubject = subject
        if (Subject.values().any { it.id == parts[index] }) {
            subSubject = getSubject(parts[index])
            index++
        } else if (parts[index] == "---") {
            index++
        }

        val subRoomId = parts[index]
        index++

        val typeDesc: String
        if (parts[index] == "Unterricht" || parts[index] == "Trotz") {
            typeDesc = parts[index] + parts[index + 1]
            index += 2
        } else {
            typeDesc = parts[index]
            index++
        }

//            val substitutionType = getSubstitutionType(typeDesc)

        when (typeDesc) {
            "frei" -> subSubject = Subject.CANCELED
        }

        var infoText = ""
        if (index < parts.size) {
            val textParts = parts.subList(index, parts.size)
            infoText = textParts.joinToString(" ")
        }

        return Substitution(
            -1,
            courses.toList(),
            lessons.toList(),
            subject,
            roomId,
            subSubject,
            subRoomId,
            typeDesc,
            infoText
        )
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
}