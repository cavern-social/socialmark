package org.timmc.socialmark

import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.timmc.socialmark.internal.SMLexer
import org.timmc.socialmark.internal.SMParser
import org.timmc.socialmark.internal.SMParser.AttrValEscapeContext
import org.timmc.socialmark.internal.SMParser.AttrValRawContext
import org.timmc.socialmark.internal.SMParser.PairedElementContext
import org.timmc.socialmark.internal.SMParser.SelfClosingElementContext
import org.timmc.socialmark.internal.SMParser.Tag_attrContext
import org.timmc.socialmark.internal.SMParser.TextEscapeContext
import org.timmc.socialmark.internal.SMParser.TextNodeContext
import org.timmc.socialmark.internal.SMParser.TextRawContext
import org.timmc.socialmark.internal.SMParser.Unicode_pointContext

object Parse {
    fun parseMarkup(sml: String): Document {
        val lexer = SMLexer(CharStreams.fromString(sml))
        val parser = SMParser(CommonTokenStream(lexer))
        parser.errorHandler = BailErrorStrategy()
        return Document.from(parser.document())
    }
}

data class Document(
    val nodes: List<Node>
) {
    companion object {
        fun from(ctx: SMParser.DocumentContext): Document {
            return Document(ctx.nodes.map(Node::from))
        }
    }
}

sealed interface Node {
    data class Text(
        val text: String,
    ) : Node {
        companion object {
            fun from(ctx: TextNodeContext): Text {
                return Text(ctx.text_pieces.joinToString("") { piece ->
                    when (piece) {
                        is TextRawContext -> piece.text
                        is TextEscapeContext -> parseEscape(piece.unicode_point())
                        else -> throw Exception("Unexpected text type: ${piece.javaClass}")
                    }
                })
            }
        }
    }

    data class Paired(
        val name: String,
        val attrs: List<Attr>, // TODO Turn into map; validate no repeated keys
        val children: List<Node>,
    ): Node {
        companion object {
            fun from(nodeCtx: PairedElementContext): Paired {
                val tagName = nodeCtx.tag_props().tag_name().text
                val attrs = nodeCtx.tag_props().attrs.map(Attr::from)
                val children = nodeCtx.inner_nodes.map(Node::from)
                if (nodeCtx.closing_name.text != tagName) {
                    throw Exception("Mismatched start and end tags (or nesting error)")
                }
                return Paired(name=tagName, attrs=attrs, children=children)
            }
        }
    }

    data class SelfClose(
        val name: String,
        val attrs: List<Attr>,
    ): Node {
        companion object {
            fun from(nodeCtx: SelfClosingElementContext): SelfClose {
                val tagName = nodeCtx.tag_props().tag_name().text
                val attrs = nodeCtx.tag_props().attrs.map(Attr::from)
                return SelfClose(name=tagName, attrs=attrs)
            }
        }
    }

    companion object {
        fun from(nodeCtx: SMParser.NodeContext): Node {
            return when (nodeCtx) {
                is TextNodeContext -> Text.from(nodeCtx)
                is PairedElementContext -> Paired.from(nodeCtx)
                is SelfClosingElementContext -> SelfClose.from(nodeCtx)
                else -> throw Exception("Unexpected node type: ${nodeCtx.javaClass}")
            }
        }
    }
}

fun parseEscape(codepoint: Unicode_pointContext): String {
    return Character.toString(Integer.parseInt(codepoint.text, 16))
}

data class Attr(
    val name: String,
    val value: String,
) {
    companion object {
        fun from(ctx: Tag_attrContext): Attr {
            return Attr(
                name=ctx.attr_name().text,
                value=ctx.attr_value().attr_value_pieces.map { piece ->
                    when (piece) {
                        is AttrValRawContext -> piece.text
                        is AttrValEscapeContext -> parseEscape(piece.unicode_point())
                        else -> throw Exception("Unexpected attr value type: ${piece.javaClass}")
                    }
                }.joinToString("")
            )
        }
    }
}
