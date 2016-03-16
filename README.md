XPath Parser
============
[![Build Status](https://travis-ci.org/exquery/xpath2-parser.png?branch=master)](https://travis-ci.org/exquery/xpath2-parser) [![Java 6+](https://img.shields.io/badge/java-6+-blue.svg)](http://java.oracle.com) [![License](https://img.shields.io/badge/license-GPL%202-blue.svg)](https://www.gnu.org/licenses/gpl-2.0.html)

This library implements a complete XPath 2 Parser in Java 1.6, using [Parboiled](https://github.com/sirthias/parboiled).

The parser generates an AST (Abstract Syntax Tree) which you can then use in your own application for whatever you wish. The class [XPathUtil](https://github.com/exquery/xpath2-parser/blob/master/src/main/java/com/evolvedbinary/xpath/parser/XPathUtil.java) shows how the parser can be used. You can also execute `XPathUtil` as an application if you want to understand the node-tree produced by the parser.

* In future it is planned to update this to XPath 3.1...
