lexer grammar SMLexer;

// Default mode: Be ready for text, text escapes, or a tag

TEXT_RAW : ~( '<' | '\\' )+;
TEXT_ESC_START : ESCAPE_START -> pushMode(ESCAPE);
TAG_START : '<' -> pushMode(TAG);

fragment ESCAPE_START : '\\u';


// A Unicode escape in either text node or attribute value (e.g. `\u2122;`)
mode ESCAPE;

HEX : [0-9A-F]+;
ESCAPE_END : ';' -> popMode;


// An open, closing, or self-closing tag
mode TAG;

CLOSE : '/';
NAME : [a-z]+; // name of tag or attribute
WS : [ \r\t\n];
ATTR_VAL_START : '="' -> pushMode(ATTR_VAL);
TAG_END : '>' -> popMode;


// A tag attribute's quoted value
mode ATTR_VAL;

ATTR_RAW : ~( '"' | '\\' | '\r' | '\n' )+;
ATTR_ESC_START : ESCAPE_START -> pushMode(ESCAPE);
ATTR_VAL_END : '"' -> popMode;

