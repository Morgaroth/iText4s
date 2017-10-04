package ca.vorona.iext.dsl

import ca.vorona.iext.dsl.Align.AlignmentType
import ca.vorona.iext.dsl.Border.BorderType

import scala.language.implicitConversions

case class CommonConst(
                        borderType: Option[BorderType],
                        alignment: Option[AlignmentType]
                      )

object Common {
  implicit def common2border(c: CommonConst) = c.borderType.get

  implicit def common2align(c: CommonConst) = c.alignment.get

  val CLeft = CommonConst(Some(Border.Left), Some(Align.Left))
  val CRight = CommonConst(Some(Border.Right), Some(Align.Right))
  val CBottom = CommonConst(Some(Border.Bottom), Some(Align.Bottom))
  val CTop = CommonConst(Some(Border.Top), Some(Align.Top))
  val CCenter = CommonConst(None, Some(Align.Center))
  val CNone = CommonConst(Some(Border.None), None)
}
