package org.timmc.socialmark

import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.timmc.socialmark.internal.ThrowingErrorListener
import org.timmc.socialmark.internal.SMLexer
import org.timmc.socialmark.internal.SMParser
import org.timmc.socialmark.internal.SMParser.AttrValEscapeContext
import org.timmc.socialmark.internal.SMParser.AttrValRawContext
import org.timmc.socialmark.internal.SMParser.PairedElementContext
import org.timmc.socialmark.internal.SMParser.SelfClosingElementContext
import org.timmc.socialmark.internal.SMParser.Tag_propsContext
import org.timmc.socialmark.internal.SMParser.TextEscapeContext
import org.timmc.socialmark.internal.SMParser.TextRawContext
import org.timmc.socialmark.internal.SMParser.Unicode_pointContext
import kotlin.text.StringBuilder

object Parse {
    /**
     * Parse a socialmark string into a Document object.
     *
     * This parse is strict on syntax but loose on semantics (it doesn't know
     * about what elements and attributes are in the spec).
     */
    fun parseMarkup(sml: String): Document {
        val lexer = SMLexer(CharStreams.fromString(sml))
        // Remove the one that prints unrecognized characters to the console,
        // and add one that will actually throw on error. (It's weird that the
        // default behavior is to just log errors and not halt the parse...)
        val errors = ThrowingErrorListener()
        lexer.removeErrorListeners()
        lexer.addErrorListener(errors)

        val parser = SMParser(CommonTokenStream(lexer))
        parser.removeErrorListeners()
        parser.addErrorListener(errors)
        parser.errorHandler = BailErrorStrategy()

        return Document.from(parser.document()) // includes EOF
    }
}

/**
 * A socialmark document, containing zero or more text or element nodes.
 */
data class Document(
    val nodes: List<Node>
) {
    fun format(): String {
        return nodes.joinToString("") { it.format() }
    }

    companion object {
        internal fun from(ctx: SMParser.DocumentContext): Document {
            return Document(Node.listFrom(ctx.nodes))
        }
    }
}

fun <T, C> List<T>.chunkBy(classifier: (T) -> C): List<List<T>> {
    val chunks = mutableListOf<List<T>>()
    var lastChunk = mutableListOf<T>()
    var lastClass: C? = null

    for (next in this) {
        val nextClass = classifier(next)
        if (lastClass == null || nextClass == lastClass) {
            lastChunk.add(next)
        } else {
            chunks.add(lastChunk.toList())
            lastChunk = mutableListOf(next)
        }
        lastClass = nextClass
    }

    if (lastChunk.isNotEmpty())
        chunks.add(lastChunk)

    return chunks.toList()
}

/**
 * Abstraction for "something in the document hierarchy".
 *
 * This can be a text node ([TextNode]) or an element ([PairedEl],
 * [SelfClosingEl]).
 */
sealed interface Node {
    fun format(): String

    companion object {
        internal fun listFrom(nodelistCtx: List<SMParser.NodeContext>): List<Node> {
            return coalesceTextNodes(nodelistCtx.map(::from))
        }

        private fun from(nodeCtx: SMParser.NodeContext): Node {
            return when (nodeCtx) {
                is TextRawContext -> TextNode.from(nodeCtx)
                is TextEscapeContext -> TextNode.from(nodeCtx)
                is PairedElementContext -> PairedEl.from(nodeCtx)
                is SelfClosingElementContext -> SelfClosingEl.from(nodeCtx)
                else -> throw Exception("Unexpected node type: ${nodeCtx.javaClass}")
            }
        }

        private fun coalesceTextNodes(nodes: List<Node>): List<Node> {
            return nodes
                .chunkBy { it is TextNode }
                .flatMap { group ->
                    if (group[0] is TextNode) {
                        @Suppress("UNCHECKED_CAST")
                        val textGroup = group as List<TextNode>
                        val contents = textGroup
                            .map(TextNode::text)
                            .joinToString("")
                        listOf(TextNode(contents))
                    } else {
                        group
                    }
                }
        }
    }
}

/**
 * A part of the document containing just unstructured text.
 */
data class TextNode(
    /** Contents of the node. Should be non-empty. */
    val text: String,
) : Node {
    override fun format(): String {
        return escapeFor(unsafeTextChars, text)
    }

    companion object {
        private val unsafeTextChars = setOf('<', '\\')

        internal fun from(rawCtx: TextRawContext): TextNode {
            return TextNode(rawCtx.text)
        }

        internal fun from(escapeCtx: TextEscapeContext): TextNode {
            return TextNode(parseEscape(escapeCtx.unicode_point()))
        }
    }
}

/**
 * An element that uses a pair of opening and closing tags.
 */
data class PairedEl(
    val name: String,
    val attrs: Map<String, String> = emptyMap(),
    val children: List<Node> = emptyList(),
): Node {
    // TODO validate name and attrs in constructor?

    override fun format(): String {
        val inner = children.joinToString("") { it.format() }
        return "<$name${Attrs.format(attrs)}>$inner</$name>"
    }

    companion object {
        internal fun from(nodeCtx: PairedElementContext): PairedEl {
            val tagName = parseElementName(nodeCtx.tag_props())
            val attrs = Attrs.from(nodeCtx.tag_props())
            val children = Node.listFrom(nodeCtx.inner_nodes)

            if (tagName != nodeCtx.closing_name.text) {
                throw Exception("Mismatched start and end tags (or nesting error)")
            }

            return PairedEl(name=tagName, attrs=attrs, children=children)
        }
    }
}

/**
 * An element that consists of a single self-closing tag.
 */
data class SelfClosingEl(
    val name: String,
    val attrs: Map<String, String> = emptyMap(),
): Node {
    override fun format(): String {
        return "<$name${Attrs.format(attrs)}/>"

    }

    companion object {
        internal fun from(nodeCtx: SelfClosingElementContext): SelfClosingEl {
            val tagName = parseElementName(nodeCtx.tag_props())
            val attrs = Attrs.from(nodeCtx.tag_props())
            return SelfClosingEl(name=tagName, attrs=attrs)
        }
    }
}

internal fun parseEscape(codepoint: Unicode_pointContext): String {
    return Character.toString(Integer.parseInt(codepoint.text, 16))
}

/**
 * Escape unsafe characters with `\u3C;` escapes.
 *
 * @param [unsafeChars] Set of ASCII characters that must be escaped.
 */
internal fun escapeFor(unsafeChars: Set<Char>, text: String): String {
    val unsafeCodepoints = unsafeChars.map { it.code }
    val ret = StringBuilder()
    // There's probably a more efficient way to do this...
    text.codePoints().forEach { cp ->
        if (unsafeCodepoints.contains(cp)) {
            ret.append("\\u")
            ret.append(Integer.toHexString(cp))
            ret.append(';')
        } else {
            ret.append(Character.toString(cp))
        }
    }
    return ret.toString()
}

internal fun parseElementName(ctx: Tag_propsContext): String {
    return ctx.tag_name().text
}

internal object Attrs {
    fun from(ctx: Tag_propsContext): Map<String, String> {
        val pairs = ctx.attrs.map { attrCtx ->
            val name = attrCtx.attr_name().text
            val value = attrCtx.attr_value().attr_value_pieces.joinToString("") { piece ->
                when (piece) {
                    is AttrValRawContext -> piece.text
                    is AttrValEscapeContext -> parseEscape(piece.unicode_point())
                    else -> throw Exception("Unexpected attr value type: ${piece.javaClass}")
                }
            }
            name to value
        }

        val names = pairs.map { it.first }
        if (names.hasDuplicates()) {
            throw Exception("Element had multiple attributes with same name")
        }

        return pairs.toMap()
    }

    val unsafeAttrChars = setOf('"', '\\', '\r', '\n')

    fun format(attrs: Map<String, String>): String {
        return attrs.map { (k, v) -> " $k=\"${escapeFor(unsafeAttrChars, v)}\"" }
            .joinToString("")
    }
}

fun List<*>.hasDuplicates(): Boolean {
    return size != toSet().size
}
