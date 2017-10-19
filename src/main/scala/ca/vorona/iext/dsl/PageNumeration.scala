package ca.vorona.iext.dsl

import com.itextpdf.text._
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfTemplate
import com.itextpdf.text.pdf.PdfWriter

class PageNumeration(
                      font: BaseFont,
                      pageTextFormatter: (Int) => String = x => s"Page $x of",
                      footerFontSize: Int = 8
                    ) extends PdfPageEventHelper {

  /** The template with the total number of pages. */
  var total: PdfTemplate = _
  private val normal: Font = new Font(font, footerFontSize)

  /**
    * Creates the PdfTemplate that will hold the total number of pages.
    *
    * @see com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(
    *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
    */
  override def onOpenDocument(writer: PdfWriter, document: Document) = {
    total = writer.getDirectContent.createTemplate(30, 12)
  }

  /**
    * Adds a header to every page
    *
    * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(
    *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
    */
  override def onEndPage(writer: PdfWriter, document: Document) = {
    val table = new PdfPTable(3)
    try {
      table.setWidths(Array[Int](24, 24, 2))
      table.getDefaultCell.setFixedHeight(10)
      table.getDefaultCell.setBorder(Rectangle.TOP)
      var cell = new PdfPCell
      cell.setBorder(0)
      cell.setBorderWidthTop(1)
      cell.setHorizontalAlignment(Element.ALIGN_LEFT)
      //      cell.setPhrase(new Phrase("some text", normalSmall))
      table.addCell(cell)
      cell = new PdfPCell
      cell.setBorder(0)
      cell.setBorderWidthTop(1)
      cell.setHorizontalAlignment(Element.ALIGN_RIGHT)
      cell.setPhrase(new Phrase(pageTextFormatter(writer.getPageNumber), normal))
      table.addCell(cell)
      cell = new PdfPCell(Image.getInstance(total))
      cell.setBorder(0)
      cell.setBorderWidthTop(1)
      table.addCell(cell)
      table.setTotalWidth(document.getPageSize.getWidth - document.leftMargin - document.rightMargin)
      table.writeSelectedRows(0, -1, document.leftMargin, document.bottomMargin - 15, writer.getDirectContent)
    } catch {
      case de: DocumentException =>
        throw new ExceptionConverter(de)
    }
  }

  /**
    * Fills out the total number of pages before the document is closed.
    *
    * @see com.itextpdf.text.pdf.PdfPageEventHelper#onCloseDocument(
    *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
    */
  override def onCloseDocument(writer: PdfWriter, document: Document) = {
    ColumnText.showTextAligned(total, Element.ALIGN_LEFT, new Phrase(String.valueOf(writer.getPageNumber), normal), 2, 2, 0)
  }
}