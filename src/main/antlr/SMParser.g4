parser grammar SMParser;

options { tokenVocab=SMLexer; }

document : (nodes+=node)* EOF;

node : (text_pieces+=text_piece)+ # TextNode
     | open_tag (inner_nodes+=node)* close_tag # ElementNode
     ;

text_piece : TEXT_RAW # TextRaw
           | TEXT_ESCAPE_START unicode_point ESCAPE_END # TextEscape
           ;

open_tag : TAG_START tag_name (WHITESPACE+ attrs+=tag_attr)* WHITESPACE* TAG_END;
close_tag : TAG_START IS_CLOSING_TAG tag_name TAG_END;
tag_name : NAME;

tag_attr : attr_name ATTR_VAL_START attr_value ATTR_VAL_END;
attr_name : NAME;
attr_value : (attr_value_pieces+=attr_value_piece)*;
attr_value_piece : ATTR_RAW # AttrValRaw
                 | ATTR_ESCAPE_START unicode_point ESCAPE_END # AttrValEscape
                 ;

// A Unicode code point. Leading zeroes are permitted but not required.
// Must be lowercase hex, 1-6 characters (inclusive).
unicode_point : HEX;
