package org.timmc.socialmark

import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.timmc.socialmark.internal.SMGrammarLexer
import org.timmc.socialmark.internal.SMGrammarParser

object Parse {
    fun parseMarkup(sml: String) {
        val lexer = SMGrammarLexer(CharStreams.fromString(sml))
        val parser = SMGrammarParser(CommonTokenStream(lexer))
        parser.errorHandler = BailErrorStrategy()
    }
}
