package com.simats.vitalsync.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.simats.vitalsync.data.model.profile.VitalPerformanceBreakdown
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WeeklyReportPdfGenerator {

    fun generatePdf(
        context: Context,
        userName: String,
        userEmail: String,
        breakdown: VitalPerformanceBreakdown
    ): File? {
        return generatePdf(
            context = context,
            userName = userName,
            userEmail = userEmail,
            wellnessScore = breakdown.overallWellnessScore,
            wellnessStatus = breakdown.overallStatus,
            heartRate = breakdown.heartRateValue,
            heartRateStatus = breakdown.heartRateStatus,
            bp = breakdown.bpValue,
            bpStatus = breakdown.bpStatus,
            spo2 = breakdown.spo2Value,
            spo2Status = breakdown.spo2Status,
            sleep = breakdown.sleepValue,
            sleepStatus = breakdown.sleepStatus,
            hydration = breakdown.hydrationValue,
            hydrationStatus = breakdown.hydrationStatus,
            steps = breakdown.activityValue,
            stepsStatus = breakdown.activityStatus,
            observationText = breakdown.observationText
        )
    }

    fun generatePdf(
        context: Context,
        userName: String,
        userEmail: String,
        wellnessScore: Int,
        wellnessStatus: String,
        heartRate: String,
        heartRateStatus: String,
        bp: String,
        bpStatus: String,
        spo2: String,
        spo2Status: String,
        sleep: String,
        sleepStatus: String,
        hydration: String,
        hydrationStatus: String,
        steps: String,
        stepsStatus: String,
        observationText: String
    ): File? {
        return try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595f
            val pageHeight = 842f
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            val leftMargin = 36f
            val rightMargin = 559f // pageWidth - 36f
            val contentWidth = 523f // rightMargin - leftMargin

            val tealColor = 0xFF00A884.toInt()
            val darkTextColor = 0xFF111827.toInt()
            val mutedTextColor = 0xFF6B7280.toInt()
            val cardBgColor = 0xFFF9FAFB.toInt()
            val borderCardColor = 0xFFE5E7EB.toInt()

            val paint = Paint().apply { isAntiAlias = true }

            // Top Header Banner
            paint.color = tealColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, pageWidth, 90f, paint)

            // Header Title
            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 22f
            canvas.drawText("VitalSync Health", leftMargin, 44f, paint)

            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("WEEKLY COMPREHENSIVE CLINICAL HEALTH REPORT", leftMargin, 68f, paint)

            // Patient Meta Box
            val patientY = 118f
            paint.textAlign = Paint.Align.LEFT
            paint.color = darkTextColor
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Patient: ${userName.ifBlank { "User" }}", leftMargin, patientY, paint)

            val metaBaselineY = patientY + 16f
            paint.color = mutedTextColor
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Email: ${userEmail.ifBlank { "vital@sync.health" }}", leftMargin, metaBaselineY, paint)

            val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
            val dateStr = "Report Generated: ${dateFormat.format(Date())}"
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(dateStr, rightMargin, metaBaselineY, paint)
            paint.textAlign = Paint.Align.LEFT // Reset alignment

            // Horizontal Divider
            val dividerY = metaBaselineY + 14f
            paint.color = borderCardColor
            paint.strokeWidth = 1f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(leftMargin, dividerY, rightMargin, dividerY, paint)

            // Wellness Score Banner Box
            val cardTop = dividerY + 14f
            val scoreCardHeight = 76f
            val scoreBoxRect = RectF(leftMargin, cardTop, rightMargin, cardTop + scoreCardHeight)

            paint.style = Paint.Style.FILL
            paint.color = 0xFFE8F6F0.toInt()
            canvas.drawRoundRect(scoreBoxRect, 12f, 12f, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = 0xFFBBF7D0.toInt()
            canvas.drawRoundRect(scoreBoxRect, 12f, 12f, paint)

            // Dynamic Score width measurement prevents "5500" overlap
            val scoreStr = wellnessScore.toString()
            val largeScorePaint = Paint().apply {
                isAntiAlias = true
                color = tealColor
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 32f
            }
            val subScorePaint = Paint().apply {
                isAntiAlias = true
                color = mutedTextColor
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 13f
            }

            val scoreX = leftMargin + 20f
            val scoreBaselineY = cardTop + 48f
            canvas.drawText(scoreStr, scoreX, scoreBaselineY, largeScorePaint)

            val scoreWidth = largeScorePaint.measureText(scoreStr)
            canvas.drawText("/100", scoreX + scoreWidth + 6f, scoreBaselineY, subScorePaint)

            // Overall Wellness Status & Subtitle
            paint.style = Paint.Style.FILL
            val textStartX = leftMargin + 120f

            paint.color = darkTextColor
            paint.textSize = 15f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Overall Wellness Status: $wellnessStatus", textStartX, cardTop + 33f, paint)

            paint.color = mutedTextColor
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Aggregated across weekly heart rate, SpO2, blood pressure, sleep, and activity.", textStartX, cardTop + 53f, paint)

            // Metrics Section Title
            val sectionTitleY = cardTop + scoreCardHeight + 24f
            paint.color = darkTextColor
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("WEEKLY VITAL TELEMETRY & PHYSICAL PERFORMANCE", leftMargin, sectionTitleY, paint)

            // Metrics Table
            val tableTop = sectionTitleY + 12f
            val metrics = listOf(
                Triple("Heart Rate", heartRate, heartRateStatus),
                Triple("Blood Pressure", bp, bpStatus),
                Triple("Blood Oxygen (SpO2)", spo2, spo2Status),
                Triple("Sleep Tracking", sleep, sleepStatus),
                Triple("Hydration Intake", hydration, hydrationStatus),
                Triple("Physical Activity", steps, stepsStatus)
            )

            val tableHeight = (metrics.size + 1) * 28f
            val tableRect = RectF(leftMargin, tableTop, rightMargin, tableTop + tableHeight)

            paint.style = Paint.Style.FILL
            paint.color = cardBgColor
            canvas.drawRoundRect(tableRect, 8f, 8f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = borderCardColor
            paint.strokeWidth = 1f
            canvas.drawRoundRect(tableRect, 8f, 8f, paint)

            // Table Column Positions
            val col1X = leftMargin + 16f // 52f
            val col2X = leftMargin + 154f // 190f
            val col3X = leftMargin + 294f // 330f
            val maxStatusWidth = (rightMargin - 16f) - col3X // 543f - 330f = 213f

            // Table Header
            val headerY = tableTop + 19f
            paint.style = Paint.Style.FILL
            paint.color = darkTextColor
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("METRIC", col1X, headerY, paint)
            canvas.drawText("WEEKLY AVERAGE / TOTAL", col2X, headerY, paint)
            canvas.drawText("STATUS", col3X, headerY, paint)

            // Table Header Divider
            val headerDividerY = headerY + 9f
            paint.color = borderCardColor
            paint.strokeWidth = 1f
            canvas.drawLine(leftMargin, headerDividerY, rightMargin, headerDividerY, paint)

            // Table Rows
            var currentMetricY = headerY
            metrics.forEach { (metricName, avgVal, statusVal) ->
                currentMetricY += 28f

                // Metric Name
                paint.style = Paint.Style.FILL
                paint.color = darkTextColor
                paint.textSize = 11f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(metricName, col1X, currentMetricY, paint)

                // Metric Value
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = darkTextColor
                canvas.drawText(avgVal, col2X, currentMetricY, paint)

                // Status Label with dynamic fitting and clinical colors
                val statusColor = when {
                    statusVal.contains("normal", ignoreCase = true) ||
                        statusVal.contains("optimal", ignoreCase = true) ||
                        statusVal.contains("good", ignoreCase = true) ||
                        statusVal.contains("active", ignoreCase = true) ||
                        statusVal.contains("excellent", ignoreCase = true) ||
                        statusVal.contains("restorative", ignoreCase = true) -> tealColor

                    statusVal.contains("mid", ignoreCase = true) ||
                        statusVal.contains("moderate", ignoreCase = true) ||
                        statusVal.contains("fair", ignoreCase = true) ||
                        statusVal.contains("elevated", ignoreCase = true) ||
                        statusVal.contains("stage 1", ignoreCase = true) -> 0xFFD97706.toInt()

                    statusVal.contains("low", ignoreCase = true) ||
                        statusVal.contains("poor", ignoreCase = true) ||
                        statusVal.contains("alert", ignoreCase = true) ||
                        statusVal.contains("stage 2", ignoreCase = true) ||
                        statusVal.contains("crisis", ignoreCase = true) ||
                        statusVal.contains("hypoxemia", ignoreCase = true) ||
                        statusVal.contains("hypotension", ignoreCase = true) ||
                        statusVal.contains("attention", ignoreCase = true) ||
                        statusVal.contains("dehydrated", ignoreCase = true) ||
                        statusVal.contains("lagging", ignoreCase = true) -> 0xFFDC2626.toInt()

                    else -> mutedTextColor
                }

                val statusPaint = Paint().apply {
                    isAntiAlias = true
                    color = statusColor
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textSize = 10.5f
                }

                // Auto-scale font size if needed so any long clinical label fits cleanly
                while (statusPaint.measureText(statusVal) > maxStatusWidth && statusPaint.textSize > 8f) {
                    statusPaint.textSize -= 0.5f
                }

                // If still exceeding at minimum font size, ellipsize gracefully so it never overflows
                val displayStatus = if (statusPaint.measureText(statusVal) > maxStatusWidth) {
                    var truncated = statusVal
                    while (truncated.isNotEmpty() && statusPaint.measureText("$truncated...") > maxStatusWidth) {
                        truncated = truncated.dropLast(1)
                    }
                    "$truncated..."
                } else {
                    statusVal
                }

                canvas.drawText(displayStatus, col3X, currentMetricY, statusPaint)
            }

            // Clinical / AI Doctor Observations Section
            val obsTitleY = tableRect.bottom + 22f
            paint.color = darkTextColor
            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("CLINICAL & AI DOCTOR OBSERVATIONS", leftMargin, obsTitleY, paint)

            val boxTop = obsTitleY + 10f
            val footerDividerY = pageHeight - 38f
            val maxBoxHeight = (footerDividerY - 14f) - boxTop

            val maxTextWidth = contentWidth - 32f // 491f
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            // Format observation text cleanly, respecting paragraphs and word wrapping
            val formattedLines = mutableListOf<String>()
            val paragraphs = observationText.split("\n")
            for (para in paragraphs) {
                val trimmedPara = para.trim()
                if (trimmedPara.isEmpty()) {
                    if (formattedLines.isNotEmpty()) formattedLines.add("")
                    continue
                }
                val words = trimmedPara.split(Regex("\\s+"))
                var currentLine = ""
                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (paint.measureText(testLine) > maxTextWidth) {
                        if (currentLine.isNotEmpty()) formattedLines.add(currentLine)
                        currentLine = word
                    } else {
                        currentLine = testLine
                    }
                }
                if (currentLine.isNotEmpty()) {
                    formattedLines.add(currentLine)
                }
            }

            val neededHeight = (formattedLines.size * 15f + 20f).coerceIn(60f, maxBoxHeight)
            val obsBoxRect = RectF(leftMargin, boxTop, rightMargin, boxTop + neededHeight)

            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            canvas.drawRoundRect(obsBoxRect, 8f, 8f, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = borderCardColor
            canvas.drawRoundRect(obsBoxRect, 8f, 8f, paint)

            // Draw observation text wrapped
            paint.style = Paint.Style.FILL
            paint.color = darkTextColor
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            var textY = boxTop + 18f
            for (line in formattedLines) {
                if (textY > boxTop + neededHeight - 6f) break
                canvas.drawText(line, leftMargin + 16f, textY, paint)
                textY += 15f
            }

            // Disclaimer & Footer
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = borderCardColor
            canvas.drawLine(leftMargin, footerDividerY, rightMargin, footerDividerY, paint)

            paint.style = Paint.Style.FILL
            paint.color = mutedTextColor
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            val disclaimerLine1 = "Notice: This report is generated by VitalSync for informational and clinical monitoring purposes."
            val disclaimerLine2 = "Always consult a physician for professional medical advice."

            canvas.drawText(disclaimerLine1, leftMargin, footerDividerY + 13f, paint)
            canvas.drawText(disclaimerLine2, leftMargin, footerDividerY + 24f, paint)

            pdfDoc.finishPage(page)

            val reportFile = File(context.cacheDir, "VitalSync_Weekly_Health_Report.pdf")
            val outputStream = FileOutputStream(reportFile)
            pdfDoc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDoc.close()

            reportFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun sharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "VitalSync Weekly Health Report")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(sendIntent, "Share Weekly Health Report PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not open PDF share chooser: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareTextSummary(context: Context, summaryText: String) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "VitalSync Weekly Health Summary")
                putExtra(Intent.EXTRA_TEXT, summaryText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "Share Weekly Summary").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not share summary: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

