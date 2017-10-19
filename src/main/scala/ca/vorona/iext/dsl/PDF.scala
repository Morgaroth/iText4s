package ca.vorona.iext.dsl

import java.io.{File, FileOutputStream}

import ca.vorona.iext.dsl.Align.AlignmentType
import ca.vorona.iext.dsl.Border.BorderType
import com.itextpdf.text._
import com.itextpdf.text.pdf.{PdfPCell, PdfPTable, PdfPageEvent, PdfWriter}
import com.itextpdf.text.pdf.BaseFont

import scala.collection.mutable
import scala.language.reflectiveCalls

object PDF {
  // static membervariables for the different styles
  val NORMAL = 0
  val BOLD = 1
  val ITALIC = 2
  val UNDERLINE = 4
  val STRIKETHRU = 8
  val BOLDITALIC = BOLD | ITALIC

  def noop = Unit
}

/**
  * Main class for PDF DSL
  */
abstract class PDF extends Document {

  val courier = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED)
  val basicFont = new Font(courier)

  // Define the file to save the PDF to
  def setup(path: String, footer: Option[PdfPageEvent] = None) {
    val file = new File(path)
    println(file.getCanonicalPath)
    val writer = PdfWriter.getInstance(this, new FileOutputStream(file))
    footer.foreach(writer.setPageEvent)
    open()
  }

  def setup(path: String, footer: PdfPageEvent) = setup(path, Some(footer))

  def paragraph(body: => Any, text: String = ""): Unit = {
    val state = call(body)
    val para = new Paragraph(text)
    para.setFont(basicFont)
    state.commands.map(_ (para))
    state.elements.map(para.add)
    enqueueElement(para)
  }

  def paragraph(text: String): Unit = paragraph(PDF.noop, text)

  def phrase(body: => Any, text: String = "") = {
    val state = call(body)
    val phrase = new Phrase(text)
    phrase.setFont(basicFont)
    state.commands.map(_ (phrase))
    state.elements.map(phrase.add)
    enqueueElement(phrase)
  }

  def phrase(text: String): Unit = phrase(PDF.noop, text)

  def cell(body: => Any): Unit = {
    val state = call(body)
    val cell = new PdfPCell()
    cell.setBorder(Border.None)
    state.commands.map(_ (cell))
    state.elements.map(cell.addElement)
    enqueueElement(cell)
  }

  def cell(text: String): Unit = cell(paragraph(text))

  def newline(num: Int = 1) = paragraph("\n" * num)

  def table(columns: Int)(body: => Any) = {
    val state = call(body)
    val table = new PdfPTable(columns)
    table.setWidthPercentage(100)
    state.commands.map(_ (table))
    state.elements.map {
      case cell: PdfPCell =>
        table.addCell(cell)
    }
    enqueueElement(table)
  }

  def chunk(body: => Any, text: String = "") = {
    val state = call(body)
    val chunk = new Chunk(text)
    chunk.setFont(basicFont)
    state.commands.map(_ (chunk))
    enqueueElement(chunk)
  }

  def chunk(text: String): Unit = chunk(PDF.noop, text)


  def leading(l: Float) {
    enqueueCommand(new Command("leading") {
      def apply(e: Element) {
        e.asInstanceOf[AnyRef {def setLeading(l: Float)}].setLeading(l)
      }
    })
  }

  def text(text: String) {
    enqueueCommand(new Command("text") {
      def apply(e: Element) {
        e.asInstanceOf[AnyRef {def append(s: String)}].append(text)
      }
    })
  }

  def font(size: Int = 10, family: Option[Font.FontFamily] = None, style: Int = 0, color: BaseColor = null) = {
    //    val font = new Font(family.getOrElse(Font.FontFamily.UNDEFINED), size, style)
    enqueueCommand(new Command("font") {
      def apply(e: Element) {
        val elem = e.asInstanceOf[AnyRef {
          def getFont(): Font
          def setFont(f: Font)
        }]
        val font = new Font(elem.getFont().getBaseFont, size, style)
        if (color != null) {
          font.setColor(color)
        }
        elem.setFont(font)
      }
    })
  }

  def border(borderType: BorderType = Border.Around, color: BaseColor = null) = {
    enqueueCommand(new Command("border-type") {
      def apply(e: Element) {
        e.asInstanceOf[AnyRef {def setBorder(border: Int)}].setBorder(borderType)
      }
    })
    Option(color).foreach { c =>
      enqueueCommand(new Command("bordercolor") {
        def apply(e: Element) {
          e.asInstanceOf[AnyRef {def setBorderColor(borderColor: BaseColor)}].setBorderColor(c)
        }
      })
    }
  }

  def align(align: AlignmentType): Unit = {
    enqueueCommand(new Command("align-generic") {
      def apply(e: Element) {
        e match {
          case t: PdfPTable => t.setHorizontalAlignment(align.rawValue)
          case t: PdfPCell => t.setVerticalAlignment(align.rawValue)
          case _ => e.asInstanceOf[AnyRef {def setAlignment(i: Int)}].setAlignment(align.rawValue)
        }
      }
    })
  }

  def alignHorizontal(align: AlignmentType): Unit = {
    enqueueCommand(new Command("align-generic") {
      def apply(e: Element) {
        e match {
          case t: PdfPTable => t.setHorizontalAlignment(align.rawValue)
          case t: PdfPCell => t.setHorizontalAlignment(align.rawValue)
          case _ => e.asInstanceOf[AnyRef {def setAlignment(i: Int)}].setAlignment(align.rawValue)
        }
      }
    })
  }

  def paddingBottom(f: Float): Unit = {
    enqueueCommand(new Command("padding-bottom") {
      def apply(e: Element) {
        e.asInstanceOf[AnyRef {def setPaddingBottom(paddingBottom: Float)}].setPaddingBottom(f)
      }
    })
  }

  def background(color: BaseColor) {
    enqueueCommand(new Command("background") {
      def apply(e: Element) {
        e match {
          case cell: PdfPCell => cell.setBackgroundColor(color)
          case _ => e.asInstanceOf[AnyRef {def setBackground(c: BaseColor)}].setBackground(color)
        }
      }
    })
  }

  def columnWidths(widths: Float*) {
    enqueueCommand(new Command("column-widths") {
      def apply(e: Element) {
        e.asInstanceOf[AnyRef {def setWidths(c: Array[Float])}].setWidths(widths.toArray)
      }
    })
  }

  /**
    * Converts string of rgb"0xRRGGBB" type to iText Color
    */
  implicit class ColorHelper(val sc: StringContext) {
    def rgb(args: Any*): BaseColor = {
      val colorDefHex = sc.s()
      val colorDef = if (colorDefHex.startsWith("0x")) colorDefHex.substring(2) else colorDefHex
      if (colorDef.length() != 6) {
        throw new PdfException(s"Incorrect color defined: $colorDefHex", null)
      }
      val r = Integer.parseInt(colorDef.substring(0, 2), 16)
      val g = Integer.parseInt(colorDef.substring(2, 4), 16)
      val b = Integer.parseInt(colorDef.substring(4, 6), 16)
      new BaseColor(r, g, b)
    }
  }

  /**
    * Queue that dequeues on map and enqueues back any failed to apply.
    * Also, its map returns Unit - this is necessary to be able to enqueue back items that failed to apply.
    * TODO: fix broken enqueue varargs
    */
  implicit class MapQueue[A](val o: mutable.Queue[A]) {
    def map[B](f: A => B) {
      o.dequeueAll(_ => true).foreach(safeApply(f))
    }

    def map[B](selector: A => Boolean, f: A => B) {
      o.dequeueAll(selector).map(f)
    }

    def safeApply[B](f: A => B)(item: A) {
      try {
        f(item)
      } catch {
        case e: Exception => throw new PdfException(s"Incorrect location for ${item.toString}()", e)
      }
    }

    def enqueue(elems: A): Unit = o.enqueue(elems)
  }

  abstract class Command(val name: String) {
    def apply(e: Element): Unit

    override def toString = name
  }

  private class State() {
    // Child elements - in order
    val elements: MapQueue[Element] = mutable.Queue[Element]()
    // Changes to apply to the current element
    val commands: MapQueue[Command] = mutable.Queue[Command]()
  }

  // The state of the PDF generator
  private val stateStack = mutable.Stack[State]()
  stateStack.push(new State())

  private def call(body: => Unit): State = {
    val state: State = new State()
    stateStack.push(state)
    body
    stateStack.pop
  }

  @inline
  private def enqueueElement(e: Element) {
    if (stateStack.nonEmpty) stateStack.top.elements.enqueue(e)
  }

  @inline
  private def enqueueCommand(c: Command) {
    if (stateStack.nonEmpty) stateStack.top.commands.enqueue(c)
  }

  override def close() = {
    stateStack.foreach(_.elements.map(add))
    super.close()
  }
}

class PdfException(msg: String, cause: Throwable) extends RuntimeException(msg, cause)
