parser grammar SMParser;

options { tokenVocab=SMLexer; }

document : (nodes+=node)* EOF;

// It's tempting to have an intermediate rule representing a run of text pieces
// (raw-text runs and individual escapes) but that results in ambiguity later in
// the parse tree -- for some reason during the escapes parsing. Something to do
// with `document` being a `node+` and `node` being a `text_piece+`.
//
// So the two types of text pieces are just top-level nodes at the grammar level
// but will be caolesced into a single text node in the final tree during
// translation.
node : TEXT_RAW # TextRaw
     | TEXT_ESC_START unicode_point ESCAPE_END # TextEscape

     | TAG_START tag_props WS* TAG_END
           (inner_nodes+=node)*
       TAG_START CLOSE (closing_name=tag_name) WS* TAG_END # PairedElement

     | TAG_START tag_props WS* CLOSE TAG_END # SelfClosingElement
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
// Must be uppercase hex, 1-6 characters (inclusive). Explicit repetition
// is used here to avoid having to write a semantic predicate (which is not
// portable).
unicode_point : HEX
              | HEX HEX
              | HEX HEX HEX
              | HEX HEX HEX HEX
              | HEX HEX HEX HEX HEX
              | HEX HEX HEX HEX HEX HEX
              ;
