package me.roybailey.research.kotlin.writers

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.PDPage
import org.junit.jupiter.api.Test
import be.quodlibet.boxable.datatable.DataTable
import be.quodlibet.boxable.BaseTable
import com.google.common.io.Files
import java.util.ArrayList
import org.apache.pdfbox.pdmodel.common.PDRectangle
import java.io.File


class PdfBoxTest {


    @Test
    fun `Test Apache PdfBox`() {

        val document = PDDocument()

        val courierBoldFont = PDType1Font.COURIER_BOLD
        val fontSize = 12f

        val page1 = PDPage()
        val contentStream = PDPageContentStream(document, page1)
        contentStream.beginText()
        contentStream.setFont(courierBoldFont, fontSize)
        contentStream.newLineAtOffset(150f, 750f)
        contentStream.showText("Hello PDFBox")
        contentStream.endText()
        contentStream.close()

        val page2 = PDPage()
        val data = mutableListOf<MutableList<Any?>>()
        data += mutableListOf<Any?>("Column One", "Column Two", "Column Three", "Column Four", "Column Five")

        for (i in 1..100) {
            data += mutableListOf<Any?>("Row $i Col One", "Row $i Col Two", "Row $i Col Three", "Row $i Col Four", "Row $i Col Five")
        }
        //Create a landscape page
        page2.mediaBox = PDRectangle(PDRectangle.A4.height, PDRectangle.A4.width)
        document.addPage(page2)
        //Initialize table
        val margin = 10f
        val tableWidth = page2.mediaBox.width - 2 * margin
        val yStartNewPage = page2.mediaBox.height - 2 * margin
        val bottomMargin = 0f
        val dataTable = BaseTable(yStartNewPage, yStartNewPage, bottomMargin, tableWidth, margin, document, page2, true, true)
        val t = DataTable(dataTable, page2)
        t.addListToTable(data, DataTable.HASHEADER)
        dataTable.draw()

        document.addPage(page1)
        document.save("./target/Test_Apache_PdfBox.pdf")
        document.close()
    }

    @Test
    fun `Test PdfBoxable Sample`() {
        //Initialize Document
        val doc = PDDocument()
        val page = PDPage()
        //Create a landscape page
        page.mediaBox = PDRectangle(PDRectangle.A4.height, PDRectangle.A4.width)
        doc.addPage(page)
        //Initialize table
        val margin = 10f
        val tableWidth = page.mediaBox.width - 2 * margin
        val yStartNewPage = page.mediaBox.height - 2 * margin
        val bottomMargin = 0f

        //Create the data
        val data = mutableListOf<MutableList<Any?>>()
        data += mutableListOf<Any?>("Column One", "Column Two", "Column Three", "Column Four", "Column Five")

        for (i in 1..10) {
            data += mutableListOf<Any?>("Row $i Col One", "Row $i Col Two", "Row $i Col Three", "Row $i Col Four", "Row $i Col Five")
        }

        val dataTable = BaseTable(yStartNewPage, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true,
                true)
        val t = DataTable(dataTable, page)
        t.addListToTable(data, DataTable.HASHEADER)
        dataTable.draw()
        val file = File("target/Test_PdfBoxable_Sample.pdf")
        System.out.println("Sample file saved at : " + file.absolutePath)
        Files.createParentDirs(file)
        doc.save(file)
        doc.close()
    }
}

