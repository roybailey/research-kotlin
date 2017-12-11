package me.roybailey.research.kotlin.writers

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.junit.Test
import java.io.FileOutputStream
import java.util.*


class ExcelPoiTest {

    @Test
    fun `Test Apache POI Excel Sample`() {
        val wb = HSSFWorkbook()
        //Workbook wb = new XSSFWorkbook();
        val createHelper = wb.creationHelper
        val sheet = wb.createSheet("new sheet")

        // Create a row and put some cells in it. Rows are 0 based.
        val row = sheet.createRow(0)

        // Create a cell and put a date value in it.  The first cell is not styled
        // as a date.
        var cell = row.createCell(0)
        cell.setCellValue(Date())

        // we style the second cell as a date (and time).  It is important to
        // create a new cell style from the workbook otherwise you can end up
        // modifying the built in style and effecting not only this cell but other cells.
        val cellStyle = wb.createCellStyle()
        cellStyle.dataFormat = createHelper.createDataFormat().getFormat("m/d/yy h:mm")
        cell = row.createCell(1)
        cell.setCellValue(Date())
        cell.setCellStyle(cellStyle)

        //you can also set date as java.util.Calendar
        cell = row.createCell(2)
        cell.setCellValue(Calendar.getInstance())
        cell.setCellStyle(cellStyle)

        // Write the output to a file
        val fileOut = FileOutputStream("./target/ExcelPoiTest.xls")
        wb.write(fileOut)
        fileOut.close()
    }
}
