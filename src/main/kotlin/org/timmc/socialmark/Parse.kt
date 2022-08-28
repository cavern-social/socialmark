package org.timmc.socialmark

import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.timmc.socialmark.internal.SMLexer
import org.timmc.socialmark.internal.SMParser
import org.timmc.socialmark.internal.SMParser.AttrValEscapeContext
import org.timmc.socialmark.internal.SMParser.AttrValRawContext
import org.timmc.socialmark.internal.SMParser.ElementNodeContext
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
    data class TextNode(
        val text: String,
    ) : Node {
        companion object {
            fun from(ctx: TextNodeContext): TextNode {
                return TextNode(ctx.text_pieces.joinToString("") { piece ->
                    when (piece) {
                        is TextRawContext -> piece.text
                        is TextEscapeContext -> parseEscape(piece.unicode_point())
                        else -> throw Exception("Unexpected text type: ${piece.javaClass}")
                    }
                })
            }
        }
    }

    class ElNode(
        val name: String,
        val attrs: List<Attribute>,
        val children: List<Node>,
    ): Node {
        companion object {
            fun from(nodeCtx: ElementNodeContext): ElNode {
                val tagName = nodeCtx.open_tag().tag_name().text
                val attrs = nodeCtx.open_tag().attrs.map(Attribute::from)
                val children = nodeCtx.inner_nodes.map(Node::from)
                return ElNode(name=tagName, attrs=attrs, children=children)
            }
        }
    }

    companion object {
        fun from(nodeCtx: SMParser.NodeContext): Node {
            return when (nodeCtx) {
                is TextNodeContext -> TextNode.from(nodeCtx)
                is ElementNodeContext -> ElNode.from(nodeCtx)
                else -> throw Exception("Unexpected node type: ${nodeCtx.javaClass}")
            }
        }
    }
}

fun parseEscape(codepoint: Unicode_pointContext): String {
    val hex = codepoint.text
    if (hex.length > 6) {
        throw Exception("Unicode escape sequence capped at 6 hex characters, was ${hex.length}")
    } else {
        return Character.toString(Integer.parseInt(hex, 16))
    }
}

data class Attribute(
    val name: String,
    val value: String,
) {
    companion object {
        fun from(ctx: Tag_attrContext): Attribute {
            return Attribute(
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
