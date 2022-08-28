grammar SMGrammar;

@header {
    package org.timmc.socialmark.internal;
}

// Parser rules

document : node* EOF;

node : text
     | open_tag node* close_tag
     ;

text : TEXT_CHAR_SAFE+;

open_tag : '<' tag_name (WHITESPACE+ attrs+=tag_attr)* WHITESPACE* '>';
close_tag : '</' tag_name '>';
tag_name : LOW_ALPHA;

tag_attr : attr_name '=' '"' attr_value '"';
attr_name : LOW_ALPHA+;
attr_value : (attr_unescaped | escape)*;

// A run of "safe" text that doesn't require escapes.
attr_unescaped : ATTR_CHARS_SAFE+;

// A Unicode escape sequence, e.g. \u61; for 'a'.
escape : '\\' 'u' unicode_point ';';

// A Unicode code point. Leading zeroes are permitted but not required.
// Must be lowercase hex, 1-6 characters (inclusive).
unicode_point : (hex+=LOW_HEX)+ { $hex.size() <= 6 }?;

// Lexer rules

LOW_ALPHA : [a-z];
WHITESPACE : [ \r\t\n];
ATTR_CHARS_SAFE : [^"\\\r\n];
LOW_HEX : [0-9a-f];
TEXT_CHAR_SAFE : [^<];
