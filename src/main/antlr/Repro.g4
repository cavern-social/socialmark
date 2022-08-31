grammar Repro;

escape : '\\u' HEX+ ';'  EOF;

HEX : [0-9A-F];
