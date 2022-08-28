lexer grammar SMLexer;

// Default mode: Be ready for text, text escapes, or a tag

TEXT_RAW : ~( '<' | '\\' )+;
TEXT_ESCAPE_START : ESC_START -> pushMode(ESCAPE);
TAG_START : '<' -> pushMode(TAG);

fragment ESC_START : '\\u';

mode ESCAPE;

HEX : [0-9A-F]+;
ESCAPE_END : ';' -> popMode;

mode TAG;

CLOSE : '/';
NAME : [a-z]+; // name of tag or attribute
TAG_END : '>' -> popMode;
WS : [ \r\t\n];
ATTR_VAL_START : '="' -> pushMode(ATTR_VAL);

mode ATTR_VAL;

ATTR_RAW : ~( '"' | '\\' | '\r' | '\n' )+;
ATTR_ESCAPE_START : ESC_START -> pushMode(ESCAPE);
ATTR_VAL_END : '"' -> popMode;

