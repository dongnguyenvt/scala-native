package scala.scalanative
package nir

import scala.collection.mutable
import nir.Attr._

sealed abstract class Attr {
  final def show: String = nir.Show(this)
}

object Attr {
  sealed abstract class Inline extends Attr
  case object MayInline extends Inline // no information
  case object InlineHint extends Inline // user hinted at inlining
  case object NoInline extends Inline // should never inline
  case object AlwaysInline extends Inline // should always inline

  sealed abstract class Specialize extends Attr
  case object MaySpecialize extends Specialize
  case object NoSpecialize extends Specialize

  sealed abstract class Opt extends Attr
  case object UnOpt extends Opt
  case object NoOpt extends Opt
  case object DidOpt extends Opt
  final case class BailOpt(msg: String) extends Opt

  case object Dyn extends Attr
  case object Stub extends Attr
  case object Extern extends Attr
  final case class Link(name: String) extends Attr
  case object Abstract extends Attr
}

final case class Attrs(
    inlineHint: Inline = MayInline,
    specialize: Specialize = MaySpecialize,
    opt: Opt = UnOpt,
    isExtern: Boolean = false,
    isDyn: Boolean = false,
    isStub: Boolean = false,
    isAbstract: Boolean = false,
    links: Seq[Attr.Link] = Seq.empty
) {
  def toSeq: Seq[Attr] = {
    val out = Seq.newBuilder[Attr]

    if (inlineHint != MayInline) out += inlineHint
    if (specialize != MaySpecialize) out += specialize
    if (opt != UnOpt) out += opt
    if (isExtern) out += Extern
    if (isDyn) out += Dyn
    if (isStub) out += Stub
    if (isAbstract) out += Abstract
    out ++= links

    out.result()
  }
}
object Attrs {
  val None = new Attrs()

  def fromSeq(attrs: Seq[Attr]): Attrs = {
    var inline = None.inlineHint
    var specialize = None.specialize
    var opt = None.opt
    var isExtern = false
    var isDyn = false
    var isStub = false
    var isAbstract = false
    val links = Seq.newBuilder[Attr.Link]

    attrs.foreach {
      case attr: Inline     => inline = attr
      case attr: Specialize => specialize = attr
      case attr: Opt        => opt = attr
      case Extern           => isExtern = true
      case Dyn              => isDyn = true
      case Stub             => isStub = true
      case link: Attr.Link  => links += link
      case Abstract         => isAbstract = true
    }

    new Attrs(
      inlineHint = inline,
      specialize = specialize,
      opt = opt,
      isExtern = isExtern,
      isDyn = isDyn,
      isStub = isStub,
      isAbstract = isAbstract,
      links = links.result()
    )
  }
}
