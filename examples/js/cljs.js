goog.addDependency("base.js", ['goog'], []);
goog.addDependency("../cljs/core.js", ['cljs.core'], ['goog.string', 'goog.array', 'goog.object', 'goog.string.StringBuffer']);
goog.addDependency("../ezglib/core.js", ['ezglib.core'], ['cljs.core']);
goog.addDependency("../basic/core.js", ['basic.core'], ['cljs.core', 'ezglib.core']);