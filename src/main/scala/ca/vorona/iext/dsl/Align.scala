package ca.vorona.iext.dsl

import com.itextpdf.text.Element

object Align {

  sealed trait AlignmentType {
    def rawValue: Int
  }

  object Left extends AlignmentType {
    val rawValue = Element.ALIGN_LEFT
  }

  object Right extends AlignmentType {
    val rawValue = Element.ALIGN_RIGHT
  }

  object Bottom extends AlignmentType {
    val rawValue = Element.ALIGN_BOTTOM
  }

  object Top extends AlignmentType {
    val rawValue = Element.ALIGN_TOP
  }

  object Center extends AlignmentType {
    val rawValue = Element.ALIGN_CENTER
  }
  object Middle extends AlignmentType {
    val rawValue = Element.ALIGN_MIDDLE
  }

  object None extends AlignmentType {
    val rawValue = -1
  }

}