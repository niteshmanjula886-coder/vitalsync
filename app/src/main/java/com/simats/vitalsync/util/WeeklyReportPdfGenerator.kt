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
            val pageWidth = 595
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            val tealColor = 0xFF00A884.toInt()
            val darkTextColor = 0xFF111827.toInt()
            val mutedTextColor = 0xFF6B7280.toInt()
            val cardBgColor = 0xFFF9FAFB.toInt()
            val borderCardColor = 0xFFE5E7EB.toInt()

            val paint = Paint().apply { isAntiAlias = true }

            // Top Header Banner
            paint.color = tealColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, paint)

            // Header Title
            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 22f
            canvas.drawText("VitalSync Health", 36f, 44f, paint)

            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("WEEKLY COMPREHENSIVE CLINICAL HEALTH REPORT", 36f, 68f, paint)

            // Patient Meta Box
            var y = 120f
            paint.color = darkTextColor
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Patient: ${userName.ifBlank { "User" }}", 36f, y, paint)

            paint.color = mutedTextColor
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Email: ${userEmail.ifBlank { "vital@sync.health" }}", 36f, y + 16f, paint)

            val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
            val dateStr = "Report Generated: ${dateFormat.format(Date())}"
            canvas.drawText(dateStr, pageWidth - 36f - paint.measureText(dateStr), y + 16f, paint)

            // Horizontal Divider
            y += 32f
            paint.color = borderCardColor
            paint.strokeWidth = 1f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(36f, y, pageWidth - 36f, y, paint)

            // Wellness Score Banner Box
            y += 18f
            val scoreBoxRect = RectF(36f, y, pageWidth - 36f, y + 80f)
            paint.style = Paint.Style.FILL
            paint.color = 0xFFE8F6F0.toInt()
            canvas.drawRoundRect(scoreBoxRect, 12f, 12f, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = 0xFFBBF7D0.toInt()
            canvas.drawRoundRect(scoreBoxRect, 12f, 12f, paint)

            paint.style = Paint.Style.FILL
            paint.color = tealColor
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 34f
            canvas.drawText("$wellnessScore", 56f, y + 52f, paint)

            paint.color = mutedTextColor
            paint.textSize = 14f
            canvas.drawText("/ 100", 56f + paint.measureText("$wellnessScore") + 6f, y + 52f, paint)

            paint.color = darkTextColor
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Overall Wellness Status: $wellnessStatus", 160f, y + 36f, paint)

            paint.color = mutedTextColor
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Aggregated across weekly heart rate, SpO2, blood pressure, sleep, and activity.", 160f, y + 56f, paint)

            // Metrics Section Title
            y += 110f
            paint.color = darkTextColor
            paint.textSize = 15f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("WEEKLY VITAL TELEMETRY & PHYSICAL PERFORMANCE", 36f, y, paint)

            // Metrics Table
            y += 14f
            val metrics = listOf(
                Triple("Heart Rate", heartRate, heartRateStatus),
                Triple("Blood Pressure", bp, bpStatus),
                Triple("Blood Oxygen (SpO2)", spo2, spo2Status),
                Triple("Sleep Tracking", sleep, sleepStatus),
                Triple("Hydration Intake", hydration, hydrationStatus),
                Triple("Physical Activity", steps, stepsStatus)
            )

            val tableRect = RectF(36f, y, pageWidth - 36f, y + (metrics.size + 1) * 28f)
            paint.style = Paint.Style.FILL
            paint.color = cardBgColor
            canvas.drawRoundRect(tableRect, 8f, 8f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = borderCardColor
            paint.strokeWidth = 1f
            canvas.drawRoundRect(tableRect, 8f, 8f, paint)

            // Table Header
            val rowY = y + 20f
            paint.style = Paint.Style.FILL
            paint.color = darkTextColor
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("METRIC", 52f, rowY, paint)
            canvas.drawText("WEEKLY AVERAGE / TOTAL", 220f, rowY, paint)
            canvas.drawText("STATUS", 420f, rowY, paint)

            var currentMetricY = rowY + 12f
            paint.color = borderCardColor
            paint.strokeWidth = 1f
            canvas.drawLine(36f, currentMetricY, pageWidth - 36f, currentMetricY, paint)

            metrics.forEach { (metricName, avgVal, statusVal) ->
                currentMetricY += 24f
                paint.style = Paint.Style.FILL
                paint.color = darkTextColor
                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(metricName, 52f, currentMetricY, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = darkTextColor
                canvas.drawText(avgVal, 220f, currentMetricY, paint)

                val statusColor = when (statusVal.lowercase(Locale.ROOT)) {
                    "normal", "optimal", "good", "active" -> tealColor
                    "mid", "moderate" -> 0xFFD97706.toInt()
                    "low", "poor" -> 0xFFDC2626.toInt()
                    else -> mutedTextColor
                }
                paint.color = statusColor
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(statusVal, 420f, currentMetricY, paint)
            }

            // Clinical / AI Doctor Observations Section
            y = tableRect.bottom + 26f
            paint.color = darkTextColor
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("CLINICAL & AI DOCTOR OBSERVATIONS", 36f, y, paint)

            y += 14f
            val obsBoxRect = RectF(36f, y, pageWidth - 36f, y + 100f)
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
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            val maxTextWidth = pageWidth - 100f
            val words = observationText.split(" ")
            var line = ""
            var textY = y + 26f

            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(testLine) > maxTextWidth) {
                    canvas.drawText(line, 52f, textY, paint)
                    line = word
                    textY += 18f
                    if (textY > y + 88f) break
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty() && textY <= y + 88f) {
                canvas.drawText(line, 52f, textY, paint)
            }

            // Disclaimer & Footer
            val footerY = pageHeight - 40f
            paint.color = borderCardColor
            paint.strokeWidth = 1f
            canvas.drawLine(36f, footerY - 14f, pageWidth - 36f, footerY - 14f, paint)

            paint.color = mutedTextColor
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val disclaimer = "Notice: This report is generated by VitalSync for informational and clinical monitoring purposes. Always consult a physician for medical advice."
            canvas.drawText(disclaimer, 36f, footerY, paint)

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

