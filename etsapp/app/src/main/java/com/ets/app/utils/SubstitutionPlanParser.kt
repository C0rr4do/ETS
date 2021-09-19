package com.ets.app.utils

import com.ets.app.model.*
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.util.*

class SubstitutionPlanParser {
    companion object {
        private val password = "pennenspatz"

        fun parseSubstitutionPlan(data: ByteArray): SubstitutionPlan {
            val doc = PDDocument.load(data, password)

            val stripper = PDFTextStripper()
            stripper.startPage = 1
            stripper.endPage = doc.numberOfPages

            val text = stripper.getText(doc)
            doc.close()

            val lines = text.split("\n")

            var index = 0
            while (!lines[index].startsWith("Klasse")) {
                index++
            }

            index++
            var substitutions = lines.map { str -> parseSubstitutionLine(str) }

            return SubstitutionPlan(Date(), Date(), Date(), "", listOf(""), substitutions)
        }

        private fun parseSubstitutionLine(line: String): Substitution {
            val parts = line.split(' ')
            var index = 0;

            // course
            var course: Course
            if (!(line[0] == 'E' || line[0] == 'Q')) {
                val friendlyName = parts[0].removeRange(2, 2).trimStart('0')
                course = Course(parts[0], null, friendlyName)
                index = 1
            } else {
                course = Course(parts[0], parts[1], "${parts[0]}(${parts[1]}")
                index = 2
            }

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

            var subSubject = Subject.CANCELED
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

            val substitutionType = getSubstitutionType(typeDesc)

            var infoText = ""
            while (index < parts.size) {
                infoText += parts[index++]
            }

            return Substitution(
                -1,
                arrayOf(course),
                lessons,
                subject,
                roomId,
                subSubject,
                subRoomId,
                substitutionType,
                infoText
            )
        }

        private fun getSubject(id: String) = Subject.values().first { it.id == id }

        private fun getSubstitutionType(type: String) =
            SubstitutionType.values().first { it.description == type }
    }
}