package ca.vorona.iext.dsl

import com.itextpdf.text.Rectangle

import scala.language.implicitConversions

object Border {

  implicit def unwrap(b: BorderType) = b.rawValue

  sealed trait BorderType {
    def rawValue: Int

    def |(another: BorderType) = new BorderType {
      override def rawValue = BorderType.this.rawValue | another.rawValue
    }
  }

  object Left extends BorderType {
    val rawValue = Rectangle.LEFT
  }

  object Right extends BorderType {
    val rawValue = Rectangle.RIGHT
  }

  object Bottom extends BorderType {
    val rawValue = Rectangle.BOTTOM
  }

  object Top extends BorderType {
    val rawValue = Rectangle.TOP
  }

  object None extends BorderType {
    val rawValue = 0
  }

  val LeftRight = Left | Right
  val TopBottom = Top | Bottom
  val Around = LeftRight | TopBottom
  val WithoutLeft = TopBottom | Right
  val WithoutRight = TopBottom | Left
  val WithoutTop = LeftRight | Bottom
  val WithoutBottom = LeftRight | Top
}