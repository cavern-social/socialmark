parser grammar SMParser;

options { tokenVocab=SMLexer; }

document : (nodes+=node)* EOF;

node : (text_pieces+=text_piece)+ # TextNode
     | TAG_START tag_props WS* TAG_END (inner_nodes+=node)* TAG_START CLOSE (closing_name=tag_name) TAG_END # PairedElement
     | TAG_START tag_props WS* CLOSE TAG_END # SelfClosingElement
     ;

text_piece : TEXT_RAW # TextRaw
           | TEXT_ESC_START unicode_point ESCAPE_END # TextEscape
           ;

tag_props : tag_name (WS+ attrs+=tag_attr)*;
tag_name : NAME;

tag_attr : attr_name ATTR_VAL_START attr_value ATTR_VAL_END;
attr_name : NAME;
attr_value : (attr_value_pieces+=attr_value_piece)*;
attr_value_piece : ATTR_RAW # AttrValRaw
                 | ATTR_ESC_START unicode_point ESCAPE_END # AttrValEscape
                 ;

// A Unicode code point. Leading zeroes are permitted but not required.
// Must be lowercase hex, 1-6 characters (inclusive). Explicit repetition
// is used here to avoid having to write a semantic predicate (which is not
// portable).
unicode_point : HEX
              | HEX HEX
              | HEX HEX HEX
              | HEX HEX HEX HEX
              | HEX HEX HEX HEX HEX
              | HEX HEX HEX HEX HEX HEX
              ;
