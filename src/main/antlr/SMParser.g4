parser grammar SMParser;

options { tokenVocab=SMLexer; }

document : node* EOF;

node : text
     | open_tag node* close_tag
     ;

text : (text_unescaped | text_escape)+;
text_unescaped : TEXT_RAW;
text_escape : TEXT_ESCAPE_START unicode_point ESCAPE_END;

open_tag : TAG_START tag_name (WHITESPACE+ attrs+=tag_attr)* WHITESPACE* TAG_END;
close_tag : TAG_START IS_CLOSING_TAG tag_name TAG_END;
tag_name : NAME;

tag_attr : attr_name ATTR_VAL_START attr_value ATTR_VAL_END;
attr_name : NAME;
attr_value : (attr_unescaped | attr_escape)*;

attr_unescaped : ATTR_RAW;
attr_escape : ATTR_ESCAPE_START unicode_point ESCAPE_END;

// A Unicode code point. Leading zeroes are permitted but not required.
// Must be lowercase hex, 1-6 characters (inclusive).
unicode_point : HEX;
