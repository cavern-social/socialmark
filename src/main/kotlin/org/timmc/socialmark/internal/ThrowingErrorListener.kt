package org.timmc.socialmark.internal

import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA

import java.util.BitSet

/** Actually throw an exception on syntax errors. */
class ThrowingErrorListener : ANTLRErrorListener {
    override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?,
                             line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
        throw Exception("Error on line $line at position $charPositionInLine: $msg (cause: $e)")
    }

    override fun reportAttemptingFullContext(
        recognizer: Parser?, dfa: DFA?, startIndex: Int, stopIndex: Int,
        conflictingAlts: BitSet?, configs: ATNConfigSet?
    ) {
        // Do nothing: It's OK if the parser falls back to full-context parsing
    }

    override fun reportAmbiguity(
        recognizer: Parser?, dfa: DFA, startIndex: Int, stopIndex: Int,
        exact: Boolean, ambigAlts: BitSet?, configs: ATNConfigSet?
    ) {
        // Do nothing: Ambiguities are handled by ANTLR. They should be removed
        // from the grammar if possible, but are not a runtime error.
    }

    override fun reportContextSensitivity(
        recognizer: Parser?, dfa: DFA?, startIndex: Int, stopIndex: Int,
        prediction: Int, configs: ATNConfigSet?
    ) {
        // Do nothing: Context-sensitivity isn't great, but this does not
        // indicate an ambiguity; there was a unique result.
    }
}
