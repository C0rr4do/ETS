package com.ets.app.utils

import android.util.Log
import com.ets.app.model.*
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.util.*
import kotlin.NoSuchElementException

class SubstitutionPlanParser {
    companion object {
        private val password = "pennenspatz"

        fun parseSubstitutionPlan(data: ByteArray): SubstitutionPlan {
            val doc = PDDocument.load(data, password)

            val stripper = PDFTextStripper()
            stripper.startPage = 1
            stripper.endPage = doc.numberOfPages

            val text: String = stripper.getText(doc)
            doc.close()

            val lines = text.split("\n")
            var substitutions: MutableList<Substitution> = mutableListOf<Substitution>()

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

                    if (line.length > 0) {
                        substitutions.add(parseSubstitutionLine(line))
                    }
                }
            }

            return SubstitutionPlan(Date(), Date(), Date(), "", listOf(""), substitutions)
        }

        private fun parseSubstitutionLine(line: String): Substitution {

            // course
            var courses: Array<Course>
            val courseRegex = Regex("""((\d{3}\w\,\s*)*(\d{3}\w))|[EQ]\d/\d\s+\w+\d+""")
            val courseString = courseRegex.find(line)?.value

            if (courseString != null) {
                val courseStringParts = courseString.split(',')
                if (!(line[0] == 'E' || line[0] == 'Q')) {
                    courses = Array<Course>(courseStringParts.size, { i ->
                        Course(
                            courseStringParts[i].trim(),
                            null,
                            courseStringParts[i].trim().removeRange(2, 3).trimStart('0')
                        )
                    })
                } else {
                    courses = Array<Course>(courseStringParts.size, { i ->
                        Course("null", null, "null")
                    })

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
                courses = Array(0, { Course("null", null, "null") })
            }

            var index = 0;
            val parts = line.removeRange(0, courseString?.length ?: 0).trimStart().split(' ')

            // lessons
            var lessons: Array<Int>
            if (parts[index + 1] == "-") {
                lessons = arrayOf(parts[index].toInt(), parts[index + 2].toInt())
                index += 3
            } else {
                lessons = arrayOf(parts[index].toInt())
                index++
            }

            // subject
            var subject = getSubject(parts[index])
            index++

            // roomId
            var roomId = parts[index]
            index++


            var subSubject = subject
            if (Subject.values().any { it.id == parts[index] }) {
                subSubject = getSubject(parts[index])
                index++
            } else if (parts[index] == "---") {
                index++
            }

            var subRoomId = parts[index]
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
            val substitutionType = typeDesc

            when (substitutionType) {
                "frei" -> subSubject = Subject.CANCELED
            }

            var infoText = ""
            if (index < parts.size) {
                val textParts = parts.subList(index, parts.size)
                infoText = textParts.joinToString(" ")
            }

            return Substitution(
                -1,
                courses,
                lessons,
                subject,
                roomId,
                subSubject,
                subRoomId,
                substitutionType,
                infoText
            )
        }

        private fun getSubject(id: String): Subject {
            try {
                return Subject.values().first { it.id == id }
            } catch (e: NoSuchElementException) {
                Log.i("com.ets.app", "Subject not found: " + id);
                throw e
            }
        }

        private fun getSubstitutionType(type: String) =
            SubstitutionType.values().first { it.description == type }
    }
}