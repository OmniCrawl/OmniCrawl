"use strict";

function _slicedToArray(arr, i) { return _arrayWithHoles(arr) || _iterableToArrayLimit(arr, i) || _unsupportedIterableToArray(arr, i) || _nonIterableRest(); }

function _nonIterableRest() { throw new TypeError("Invalid attempt to destructure non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); }

function _iterableToArrayLimit(arr, i) { if (typeof Symbol === "undefined" || !(Symbol.iterator in Object(arr))) return; var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"] != null) _i["return"](); } finally { if (_d) throw _e; } } return _arr; }

function _arrayWithHoles(arr) { if (Array.isArray(arr)) return arr; }

function _createForOfIteratorHelper(o, allowArrayLike) { var it; if (typeof Symbol === "undefined" || o[Symbol.iterator] == null) { if (Array.isArray(o) || (it = _unsupportedIterableToArray(o)) || allowArrayLike && o && typeof o.length === "number") { if (it) o = it; var i = 0; var F = function F() {}; return { s: F, n: function n() { if (i >= o.length) return { done: true }; return { done: false, value: o[i++] }; }, e: function e(_e2) { throw _e2; }, f: F }; } throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); } var normalCompletion = true, didErr = false, err; return { s: function s() { it = o[Symbol.iterator](); }, n: function n() { var step = it.next(); normalCompletion = step.done; return step; }, e: function e(_e3) { didErr = true; err = _e3; }, f: function f() { try { if (!normalCompletion && it.return != null) it.return(); } finally { if (didErr) throw err; } } }; }

function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }

function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) { arr2[i] = arr[i]; } return arr2; }

function _instanceof(left, right) { if (right != null && typeof Symbol !== "undefined" && right[Symbol.hasInstance]) { return !!right[Symbol.hasInstance](left); } else { return left instanceof right; } }

function _typeof(obj) { "@babel/helpers - typeof"; if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

!function () {
  // Some website (e.g. Amazon) will evalute this injected script twice
  if (window.__injectedScriptExecuted) {
    return;
  }

  window.__injectedScriptExecuted = true; // Helper module: decycle and stacktrace

  var stackTrace = {};
  !function (e) {
    if ("object" == (typeof exports === "undefined" ? "undefined" : _typeof(exports)) && "undefined" != typeof module) module.exports = e();else if ("function" == typeof define && define.amd) define([], e);else {
      stackTrace.StackTrace = e();
    }
  }(function () {
    return function i(a, s, u) {
      function c(n, e) {
        if (!s[n]) {
          if (!a[n]) {
            var t = "function" == typeof require && require;
            if (!e && t) return t(n, !0);
            if (l) return l(n, !0);
            var r = new Error("Cannot find module '" + n + "'");
            throw r.code = "MODULE_NOT_FOUND", r;
          }

          var o = s[n] = {
            exports: {}
          };
          a[n][0].call(o.exports, function (e) {
            var t = a[n][1][e];
            return c(t || e);
          }, o, o.exports, i, a, s, u);
        }

        return s[n].exports;
      }

      for (var l = "function" == typeof require && require, e = 0; e < u.length; e++) {
        c(u[e]);
      }

      return c;
    }({
      1: [function (n, r, o) {
        !function (e, t) {
          "use strict";

          "object" == _typeof(o) ? r.exports = t(n("stackframe")) : e.ErrorStackParser = t(e.StackFrame);
        }(this, function (s) {
          "use strict";

          var t = /(^|@)\S+\:\d+/,
              n = /^\s*at .*(\S+\:\d+|\(native\))/m,
              r = /^(eval@)?(\[native code\])?$/;
          return {
            parse: function parse(e) {
              if (void 0 !== e.stacktrace || void 0 !== e["opera#sourceloc"]) return this.parseOpera(e);
              if (e.stack && e.stack.match(n)) return this.parseV8OrIE(e);
              if (e.stack) return this.parseFFOrSafari(e);
              throw new Error("Cannot parse given Error object");
            },
            extractLocation: function extractLocation(e) {
              if (-1 === e.indexOf(":")) return [e];
              var t = /(.+?)(?:\:(\d+))?(?:\:(\d+))?$/.exec(e.replace(/[\(\)]/g, ""));
              return [t[1], t[2] || void 0, t[3] || void 0];
            },
            parseV8OrIE: function parseV8OrIE(e) {
              return e.stack.split("\n").filter(function (e) {
                return !!e.match(n);
              }, this).map(function (e) {
                -1 < e.indexOf("(eval ") && (e = e.replace(/eval code/g, "eval").replace(/(\(eval at [^\()]*)|(\)\,.*$)/g, ""));
                var t = e.replace(/^\s+/, "").replace(/\(eval code/g, "("),
                    n = t.match(/ (\((.+):(\d+):(\d+)\)$)/),
                    r = (t = n ? t.replace(n[0], "") : t).split(/\s+/).slice(1),
                    o = this.extractLocation(n ? n[1] : r.pop()),
                    i = r.join(" ") || void 0,
                    a = -1 < ["eval", "<anonymous>"].indexOf(o[0]) ? void 0 : o[0];
                return new s({
                  functionName: i,
                  fileName: a,
                  lineNumber: o[1],
                  columnNumber: o[2],
                  source: e
                });
              }, this);
            },
            parseFFOrSafari: function parseFFOrSafari(e) {
              return e.stack.split("\n").filter(function (e) {
                return !e.match(r);
              }, this).map(function (e) {
                if (-1 < e.indexOf(" > eval") && (e = e.replace(/ line (\d+)(?: > eval line \d+)* > eval\:\d+\:\d+/g, ":$1")), -1 === e.indexOf("@") && -1 === e.indexOf(":")) return new s({
                  functionName: e
                });
                var t = /((.*".+"[^@]*)?[^@]*)(?:@)/,
                    n = e.match(t),
                    r = n && n[1] ? n[1] : void 0,
                    o = this.extractLocation(e.replace(t, ""));
                return new s({
                  functionName: r,
                  fileName: o[0],
                  lineNumber: o[1],
                  columnNumber: o[2],
                  source: e
                });
              }, this);
            },
            parseOpera: function parseOpera(e) {
              return !e.stacktrace || -1 < e.message.indexOf("\n") && e.message.split("\n").length > e.stacktrace.split("\n").length ? this.parseOpera9(e) : e.stack ? this.parseOpera11(e) : this.parseOpera10(e);
            },
            parseOpera9: function parseOpera9(e) {
              for (var t = /Line (\d+).*script (?:in )?(\S+)/i, n = e.message.split("\n"), r = [], o = 2, i = n.length; o < i; o += 2) {
                var a = t.exec(n[o]);
                a && r.push(new s({
                  fileName: a[2],
                  lineNumber: a[1],
                  source: n[o]
                }));
              }

              return r;
            },
            parseOpera10: function parseOpera10(e) {
              for (var t = /Line (\d+).*script (?:in )?(\S+)(?:: In function (\S+))?$/i, n = e.stacktrace.split("\n"), r = [], o = 0, i = n.length; o < i; o += 2) {
                var a = t.exec(n[o]);
                a && r.push(new s({
                  functionName: a[3] || void 0,
                  fileName: a[2],
                  lineNumber: a[1],
                  source: n[o]
                }));
              }

              return r;
            },
            parseOpera11: function parseOpera11(e) {
              return e.stack.split("\n").filter(function (e) {
                return !!e.match(t) && !e.match(/^Error created at/);
              }, this).map(function (e) {
                var t,
                    n = e.split("@"),
                    r = this.extractLocation(n.pop()),
                    o = n.shift() || "",
                    i = o.replace(/<anonymous function(: (\w+))?>/, "$2").replace(/\([^\)]*\)/g, "") || void 0;
                o.match(/\(([^\)]*)\)/) && (t = o.replace(/^[^\(]+\(([^\)]*)\)$/, "$1"));
                var a = void 0 === t || "[arguments not available]" === t ? void 0 : t.split(",");
                return new s({
                  functionName: i,
                  args: a,
                  fileName: r[0],
                  lineNumber: r[1],
                  columnNumber: r[2],
                  source: e
                });
              }, this);
            }
          };
        });
      }, {
        stackframe: 2
      }],
      2: [function (e, n, r) {
        !function (e, t) {
          "use strict";

          "object" == _typeof(r) ? n.exports = t() : e.StackFrame = t();
        }(this, function () {
          "use strict";

          function n(e) {
            return e.charAt(0).toUpperCase() + e.substring(1);
          }

          function e(e) {
            return function () {
              return this[e];
            };
          }

          function l(e) {
            if (_instanceof(e, Object)) for (var t = 0; t < i.length; t++) {
              e.hasOwnProperty(i[t]) && void 0 !== e[i[t]] && this["set" + n(i[t])](e[i[t]]);
            }
          }

          var t = ["isConstructor", "isEval", "isNative", "isToplevel"],
              r = ["columnNumber", "lineNumber"],
              o = ["fileName", "functionName", "source"],
              i = t.concat(r, o, ["args"]);
          l.prototype = {
            getArgs: function getArgs() {
              return this.args;
            },
            setArgs: function setArgs(e) {
              if ("[object Array]" !== Object.prototype.toString.call(e)) throw new TypeError("Args must be an Array");
              this.args = e;
            },
            getEvalOrigin: function getEvalOrigin() {
              return this.evalOrigin;
            },
            setEvalOrigin: function setEvalOrigin(e) {
              if (_instanceof(e, l)) this.evalOrigin = e;else {
                if (!_instanceof(e, Object)) throw new TypeError("Eval Origin must be an Object or StackFrame");
                this.evalOrigin = new l(e);
              }
            },
            toString: function toString() {
              var e = this.getFileName() || "",
                  t = this.getLineNumber() || "",
                  n = this.getColumnNumber() || "",
                  r = this.getFunctionName() || "";
              return this.getIsEval() ? e ? "[eval] (" + e + ":" + t + ":" + n + ")" : "[eval]:" + t + ":" + n : r ? r + " (" + e + ":" + t + ":" + n + ")" : e + ":" + t + ":" + n;
            }
          }, l.fromString = function (e) {
            var t = e.indexOf("("),
                n = e.lastIndexOf(")"),
                r = e.substring(0, t),
                o = e.substring(t + 1, n).split(","),
                i = e.substring(n + 1);
            if (0 === i.indexOf("@")) var a = /@(.+?)(?::(\d+))?(?::(\d+))?$/.exec(i, ""),
                s = a[1],
                u = a[2],
                c = a[3];
            return new l({
              functionName: r,
              args: o || void 0,
              fileName: s,
              lineNumber: u || void 0,
              columnNumber: c || void 0
            });
          };

          for (var a = 0; a < t.length; a++) {
            l.prototype["get" + n(t[a])] = e(t[a]), l.prototype["set" + n(t[a])] = function (t) {
              return function (e) {
                this[t] = Boolean(e);
              };
            }(t[a]);
          }

          for (var s = 0; s < r.length; s++) {
            l.prototype["get" + n(r[s])] = e(r[s]), l.prototype["set" + n(r[s])] = function (n) {
              return function (e) {
                if (t = e, isNaN(parseFloat(t)) || !isFinite(t)) throw new TypeError(n + " must be a Number");
                var t;
                this[n] = Number(e);
              };
            }(r[s]);
          }

          for (var u = 0; u < o.length; u++) {
            l.prototype["get" + n(o[u])] = e(o[u]), l.prototype["set" + n(o[u])] = function (t) {
              return function (e) {
                this[t] = String(e);
              };
            }(o[u]);
          }

          return l;
        });
      }, {}],
      3: [function (Z, n, r) {
        (function (I, z) {
          var e, t;
          e = this, t = function t() {
            "use strict";

            function c(e) {
              return "function" == typeof e;
            }

            function t() {
              var e = setTimeout;
              return function () {
                return e(n, 1);
              };
            }

            function n() {
              for (var e = 0; e < E; e += 2) {
                (0, R[e])(R[e + 1]), R[e] = void 0, R[e + 1] = void 0;
              }

              E = 0;
            }

            function a(e, t) {
              var n = arguments,
                  r = this,
                  o = new this.constructor(u);
              void 0 === o[U] && y(o);
              var i,
                  a = r._state;
              return a ? (i = n[a - 1], L(function () {
                return v(a, o, i, r._result);
              })) : d(r, o, e, t), o;
            }

            function s(e) {
              if (e && "object" == _typeof(e) && e.constructor === this) return e;
              var t = new this(u);
              return p(t, e), t;
            }

            function u() {}

            function l(e) {
              try {
                return e.then;
              } catch (e) {
                return B.error = e, B;
              }
            }

            function r(e, r, o) {
              L(function (t) {
                var n = !1,
                    e = function (e, t, n, r) {
                  try {
                    e.call(t, n, r);
                  } catch (e) {
                    return e;
                  }
                }(o, r, function (e) {
                  n || (n = !0, r !== e ? p(t, e) : h(t, e));
                }, function (e) {
                  n || (n = !0, g(t, e));
                }, t._label);

                !n && e && (n = !0, g(t, e));
              }, e);
            }

            function f(e, t, n) {
              t.constructor === e.constructor && n === a && t.constructor.resolve === s ? function (t, e) {
                e._state === G ? h(t, e._result) : e._state === D ? g(t, e._result) : d(e, void 0, function (e) {
                  return p(t, e);
                }, function (e) {
                  return g(t, e);
                });
              }(e, t) : n === B ? g(e, B.error) : void 0 === n ? h(e, t) : c(n) ? r(e, t, n) : h(e, t);
            }

            function p(e, t) {
              e === t ? g(e, new TypeError("You cannot resolve a promise with itself")) : function (e) {
                return "function" == typeof e || "object" == _typeof(e) && null !== e;
              }(t) ? f(e, t, l(t)) : h(e, t);
            }

            function o(e) {
              e._onerror && e._onerror(e._result), m(e);
            }

            function h(e, t) {
              e._state === $ && (e._result = t, e._state = G, 0 !== e._subscribers.length && L(m, e));
            }

            function g(e, t) {
              e._state === $ && (e._state = D, e._result = t, L(o, e));
            }

            function d(e, t, n, r) {
              var o = e._subscribers,
                  i = o.length;
              e._onerror = null, o[i] = t, o[i + G] = n, o[i + D] = r, 0 === i && e._state && L(m, e);
            }

            function m(e) {
              var t = e._subscribers,
                  n = e._state;

              if (0 !== t.length) {
                for (var r = void 0, o = void 0, i = e._result, a = 0; a < t.length; a += 3) {
                  r = t[a], o = t[a + n], r ? v(n, r, o, i) : o(i);
                }

                e._subscribers.length = 0;
              }
            }

            function e() {
              this.error = null;
            }

            function v(e, t, n, r) {
              var o = c(n),
                  i = void 0,
                  a = void 0,
                  s = void 0,
                  u = void 0;

              if (o) {
                if ((i = function (e, t) {
                  try {
                    return e(t);
                  } catch (e) {
                    return J.error = e, J;
                  }
                }(n, r)) === J ? (u = !0, a = i.error, i = null) : s = !0, t === i) return void g(t, new TypeError("A promises callback cannot return that same promise."));
              } else i = r, s = !0;

              t._state !== $ || (o && s ? p(t, i) : u ? g(t, a) : e === G ? h(t, i) : e === D && g(t, i));
            }

            function y(e) {
              e[U] = q++, e._state = void 0, e._result = void 0, e._subscribers = [];
            }

            function i(e, t) {
              this._instanceConstructor = e, this.promise = new e(u), this.promise[U] || y(this.promise), S(t) ? (this._input = t, this.length = t.length, this._remaining = t.length, this._result = new Array(this.length), 0 === this.length ? h(this.promise, this._result) : (this.length = this.length || 0, this._enumerate(), 0 === this._remaining && h(this.promise, this._result))) : g(this.promise, new Error("Array Methods must be provided an Array"));
            }

            function _(e) {
              this[U] = q++, this._result = this._state = void 0, this._subscribers = [], u !== e && ("function" != typeof e && function () {
                throw new TypeError("You must pass a resolver function as the first argument to the promise constructor");
              }(), _instanceof(this, _) ? function (t, e) {
                try {
                  e(function (e) {
                    p(t, e);
                  }, function (e) {
                    g(t, e);
                  });
                } catch (e) {
                  g(t, e);
                }
              }(this, e) : function () {
                throw new TypeError("Failed to construct 'Promise': Please use the 'new' operator, this object constructor cannot be called as a function.");
              }());
            }

            function w() {
              var e = void 0;
              if (void 0 !== z) e = z;else if ("undefined" != typeof self) e = self;else try {
                e = Function("return this")();
              } catch (e) {
                throw new Error("polyfill failed because global object is unavailable in this environment");
              }
              var t = e.Promise;

              if (t) {
                var n = null;

                try {
                  n = Object.prototype.toString.call(t.resolve());
                } catch (e) {}

                if ("[object Promise]" === n && !t.cast) return;
              }

              e.Promise = _;
            }

            var b,
                C,
                A,
                O,
                S = Array.isArray ? Array.isArray : function (e) {
              return "[object Array]" === Object.prototype.toString.call(e);
            },
                E = 0,
                N = void 0,
                T = void 0,
                L = function L(e, t) {
              R[E] = e, R[E + 1] = t, 2 === (E += 2) && (T ? T(n) : F());
            },
                M = "undefined" != typeof window ? window : void 0,
                j = M || {},
                x = j.MutationObserver || j.WebKitMutationObserver,
                P = "undefined" == typeof self && void 0 !== I && "[object process]" === {}.toString.call(I),
                k = "undefined" != typeof Uint8ClampedArray && "undefined" != typeof importScripts && "undefined" != typeof MessageChannel,
                R = new Array(1e3),
                F = void 0;

            F = P ? function () {
              return I.nextTick(n);
            } : x ? (C = 0, A = new x(n), O = document.createTextNode(""), A.observe(O, {
              characterData: !0
            }), function () {
              O.data = C = ++C % 2;
            }) : k ? ((b = new MessageChannel()).port1.onmessage = n, function () {
              return b.port2.postMessage(0);
            }) : void 0 === M && "function" == typeof Z ? function () {
              try {
                var e = Z("vertx");
                return N = e.runOnLoop || e.runOnContext, function () {
                  N(n);
                };
              } catch (e) {
                return t();
              }
            }() : t();
            var U = Math.random().toString(36).substring(16),
                $ = void 0,
                G = 1,
                D = 2,
                B = new e(),
                J = new e(),
                q = 0;
            return i.prototype._enumerate = function () {
              for (var e = this.length, t = this._input, n = 0; this._state === $ && n < e; n++) {
                this._eachEntry(t[n], n);
              }
            }, i.prototype._eachEntry = function (t, e) {
              var n = this._instanceConstructor,
                  r = n.resolve;

              if (r === s) {
                var o = l(t);
                if (o === a && t._state !== $) this._settledAt(t._state, e, t._result);else if ("function" != typeof o) this._remaining--, this._result[e] = t;else if (n === _) {
                  var i = new n(u);
                  f(i, t, o), this._willSettleAt(i, e);
                } else this._willSettleAt(new n(function (e) {
                  return e(t);
                }), e);
              } else this._willSettleAt(r(t), e);
            }, i.prototype._settledAt = function (e, t, n) {
              var r = this.promise;
              r._state === $ && (this._remaining--, e === D ? g(r, n) : this._result[t] = n), 0 === this._remaining && h(r, this._result);
            }, i.prototype._willSettleAt = function (e, t) {
              var n = this;
              d(e, void 0, function (e) {
                return n._settledAt(G, t, e);
              }, function (e) {
                return n._settledAt(D, t, e);
              });
            }, _.all = function (e) {
              return new i(this, e).promise;
            }, _.race = function (o) {
              var i = this;
              return new i(S(o) ? function (e, t) {
                for (var n = o.length, r = 0; r < n; r++) {
                  i.resolve(o[r]).then(e, t);
                }
              } : function (e, t) {
                return t(new TypeError("You must pass an array to race."));
              });
            }, _.resolve = s, _.reject = function (e) {
              var t = new this(u);
              return g(t, e), t;
            }, _._setScheduler = function (e) {
              T = e;
            }, _._setAsap = function (e) {
              L = e;
            }, _._asap = L, _.prototype = {
              constructor: _,
              then: a,
              catch: function _catch(e) {
                return this.then(null, e);
              }
            }, w(), _.polyfill = w, _.Promise = _;
          }, "object" == _typeof(r) && void 0 !== n ? n.exports = t() : e.ES6Promise = t();
        }).call(this, Z("_process"), "undefined" != typeof global ? global : "undefined" != typeof self ? self : "undefined" != typeof window ? window : {});
      }, {
        _process: 5
      }],
      4: [function (e, s, u) {
        (function (a) {
          (function () {
            function $(e, u) {
              function c(e) {
                if (c[e] !== E) return c[e];
                var t;
                if ("bug-string-char-index" == e) t = "a" != "a"[0];else if ("json" == e) t = c("json-stringify") && c("json-parse");else {
                  var n,
                      r = "{\"a\":[1,true,false,null,\"\\u0000\\b\\n\\f\\r\\t\"]}";

                  if ("json-stringify" == e) {
                    var o = u.stringify,
                        i = "function" == typeof o && h;

                    if (i) {
                      (n = function n() {
                        return 1;
                      }).toJSON = n;

                      try {
                        i = "0" === o(0) && "0" === o(new l()) && '""' == o(new f()) && o(N) === E && o(E) === E && o() === E && "1" === o(n) && "[1]" == o([n]) && "[null]" == o([E]) && "null" == o(null) && "[null,null,null]" == o([E, N, null]) && o({
                          a: [n, !0, !1, null, "\0\b\n\f\r\t"]
                        }) == r && "1" === o(null, n) && "[\n 1,\n 2\n]" == o([1, 2], null, 1) && '"-271821-04-20T00:00:00.000Z"' == o(new p(-864e13)) && '"+275760-09-13T00:00:00.000Z"' == o(new p(864e13)) && '"-000001-01-01T00:00:00.000Z"' == o(new p(-621987552e5)) && '"1969-12-31T23:59:59.999Z"' == o(new p(-1));
                      } catch (e) {
                        i = !1;
                      }
                    }

                    t = i;
                  }

                  if ("json-parse" == e) {
                    var a = u.parse;
                    if ("function" == typeof a) try {
                      if (0 === a("0") && !a(!1)) {
                        var s = 5 == (n = a(r)).a.length && 1 === n.a[0];

                        if (s) {
                          try {
                            s = !a('"\t"');
                          } catch (e) {}

                          if (s) try {
                            s = 1 !== a("01");
                          } catch (e) {}
                          if (s) try {
                            s = 1 !== a("1.");
                          } catch (e) {}
                        }
                      }
                    } catch (e) {
                      s = !1;
                    }
                    t = s;
                  }
                }
                return c[e] = !!t;
              }

              e = e || D.Object(), u = u || D.Object();
              var l = e.Number || D.Number,
                  f = e.String || D.String,
                  t = e.Object || D.Object,
                  p = e.Date || D.Date,
                  n = e.SyntaxError || D.SyntaxError,
                  A = e.TypeError || D.TypeError,
                  r = e.Math || D.Math,
                  o = e.JSON || D.JSON;
              "object" == _typeof(o) && o && (u.stringify = o.stringify, u.parse = o.parse);

              var _O,
                  _S,
                  E,
                  i = t.prototype,
                  N = i.toString,
                  h = new p(-0xc782b5b800cec);

              try {
                h = -109252 == h.getUTCFullYear() && 0 === h.getUTCMonth() && 1 === h.getUTCDate() && 10 == h.getUTCHours() && 37 == h.getUTCMinutes() && 6 == h.getUTCSeconds() && 708 == h.getUTCMilliseconds();
              } catch (e) {}

              if (!c("json")) {
                var g = "[object Function]",
                    T = "[object Number]",
                    L = "[object String]",
                    M = "[object Array]",
                    s = c("bug-string-char-index");
                if (!h) var j = r.floor,
                    a = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334],
                    x = function x(e, t) {
                  return a[t] + 365 * (e - 1970) + j((e - 1969 + (t = +(1 < t))) / 4) - j((e - 1901 + t) / 100) + j((e - 1601 + t) / 400);
                };

                if ((_O = i.hasOwnProperty) || (_O = function O(e) {
                  var n,
                      t = {};
                  return _O = (t.__proto__ = null, t.__proto__ = {
                    toString: 1
                  }, t).toString != N ? function (e) {
                    var t = this.__proto__,
                        n = (e in (this.__proto__ = null, this));
                    return this.__proto__ = t, n;
                  } : (n = t.constructor, function (e) {
                    var t = (this.constructor || n).prototype;
                    return e in this && !(e in t && this[e] === t[e]);
                  }), t = null, _O.call(this, e);
                }), _S = function S(e, t) {
                  var n,
                      a,
                      r,
                      o = 0;

                  for (r in (n = function n() {
                    this.valueOf = 0;
                  }).prototype.valueOf = 0, a = new n()) {
                    _O.call(a, r) && o++;
                  }

                  return n = a = null, (_S = o ? 2 == o ? function (e, t) {
                    var n,
                        r = {},
                        o = N.call(e) == g;

                    for (n in e) {
                      o && "prototype" == n || _O.call(r, n) || !(r[n] = 1) || !_O.call(e, n) || t(n);
                    }
                  } : function (e, t) {
                    var n,
                        r,
                        o = N.call(e) == g;

                    for (n in e) {
                      o && "prototype" == n || !_O.call(e, n) || (r = "constructor" === n) || t(n);
                    }

                    (r || _O.call(e, n = "constructor")) && t(n);
                  } : (a = ["valueOf", "toString", "toLocaleString", "propertyIsEnumerable", "isPrototypeOf", "hasOwnProperty", "constructor"], function (e, t) {
                    var n,
                        r,
                        o = N.call(e) == g,
                        i = !o && "function" != typeof e.constructor && G[_typeof(e.hasOwnProperty)] && e.hasOwnProperty || _O;

                    for (n in e) {
                      o && "prototype" == n || !i.call(e, n) || t(n);
                    }

                    for (r = a.length; n = a[--r]; i.call(e, n) && t(n)) {
                      ;
                    }
                  }))(e, t);
                }, !c("json-stringify")) {
                  var P = function P(e, t) {
                    return ("000000" + (t || 0)).slice(-e);
                  };

                  var k = function k(e) {
                    for (var t = '"', n = 0, r = e.length, o = !s || 10 < r, i = o && (s ? e.split("") : e); n < r; n++) {
                      var a = e.charCodeAt(n);

                      switch (a) {
                        case 8:
                        case 9:
                        case 10:
                        case 12:
                        case 13:
                        case 34:
                        case 92:
                          t += d[a];
                          break;

                        default:
                          if (a < 32) {
                            t += "\\u00" + P(2, a.toString(16));
                            break;
                          }

                          t += o ? i[n] : e.charAt(n);
                      }
                    }

                    return t + '"';
                  };

                  var d = {
                    92: "\\\\",
                    34: '\\"',
                    8: "\\b",
                    12: "\\f",
                    10: "\\n",
                    13: "\\r",
                    9: "\\t"
                  },
                      R = function R(e, t, n, r, o, i, a) {
                    var s, u, c, l, f, p, h, g, d, m, v, y, _, w, b, C;

                    try {
                      s = t[e];
                    } catch (e) {}

                    if ("object" == _typeof(s) && s) if ("[object Date]" != (u = N.call(s)) || _O.call(s, "toJSON")) "function" == typeof s.toJSON && (u != T && u != L && u != M || _O.call(s, "toJSON")) && (s = s.toJSON(e));else if (-1 / 0 < s && s < 1 / 0) {
                      if (x) {
                        for (f = j(s / 864e5), c = j(f / 365.2425) + 1970 - 1; x(c + 1, 0) <= f; c++) {
                          ;
                        }

                        for (l = j((f - x(c, 0)) / 30.42); x(c, l + 1) <= f; l++) {
                          ;
                        }

                        f = 1 + f - x(c, l), h = j((p = (s % 864e5 + 864e5) % 864e5) / 36e5) % 24, g = j(p / 6e4) % 60, d = j(p / 1e3) % 60, m = p % 1e3;
                      } else c = s.getUTCFullYear(), l = s.getUTCMonth(), f = s.getUTCDate(), h = s.getUTCHours(), g = s.getUTCMinutes(), d = s.getUTCSeconds(), m = s.getUTCMilliseconds();

                      s = (c <= 0 || 1e4 <= c ? (c < 0 ? "-" : "+") + P(6, c < 0 ? -c : c) : P(4, c)) + "-" + P(2, l + 1) + "-" + P(2, f) + "T" + P(2, h) + ":" + P(2, g) + ":" + P(2, d) + "." + P(3, m) + "Z";
                    } else s = null;
                    if (n && (s = n.call(t, e, s)), null === s) return "null";
                    if ("[object Boolean]" == (u = N.call(s))) return "" + s;
                    if (u == T) return -1 / 0 < s && s < 1 / 0 ? "" + s : "null";
                    if (u == L) return k("" + s);

                    if ("object" == _typeof(s)) {
                      for (w = a.length; w--;) {
                        if (a[w] === s) throw A();
                      }

                      if (a.push(s), v = [], b = i, i += o, u == M) {
                        for (_ = 0, w = s.length; _ < w; _++) {
                          y = R(_, s, n, r, o, i, a), v.push(y === E ? "null" : y);
                        }

                        C = v.length ? o ? "[\n" + i + v.join(",\n" + i) + "\n" + b + "]" : "[" + v.join(",") + "]" : "[]";
                      } else _S(r || s, function (e) {
                        var t = R(e, s, n, r, o, i, a);
                        t !== E && v.push(k(e) + ":" + (o ? " " : "") + t);
                      }), C = v.length ? o ? "{\n" + i + v.join(",\n" + i) + "\n" + b + "}" : "{" + v.join(",") + "}" : "{}";

                      return a.pop(), C;
                    }
                  };

                  u.stringify = function (e, t, n) {
                    var r, o, i, a;
                    if (G[_typeof(t)] && t) if ((a = N.call(t)) == g) o = t;else if (a == M) {
                      i = {};

                      for (var s, u = 0, c = t.length; u < c; s = t[u++], (a = N.call(s)) != L && a != T || (i[s] = 1)) {
                        ;
                      }
                    }
                    if (n) if ((a = N.call(n)) == T) {
                      if (0 < (n -= n % 1)) for (r = "", 10 < n && (n = 10); r.length < n; r += " ") {
                        ;
                      }
                    } else a == L && (r = n.length <= 10 ? n : n.slice(0, 10));
                    return R("", ((s = {})[""] = e, s), o, i, r, "", []);
                  };
                }

                if (!c("json-parse")) {
                  var m = function m() {
                    throw _ = w = null, n();
                  };

                  var v = function v() {
                    for (var e, t, n, r, o, i = w, a = i.length; _ < a;) {
                      switch (o = i.charCodeAt(_)) {
                        case 9:
                        case 10:
                        case 13:
                        case 32:
                          _++;
                          break;

                        case 123:
                        case 125:
                        case 91:
                        case 93:
                        case 58:
                        case 44:
                          return e = s ? i.charAt(_) : i[_], _++, e;

                        case 34:
                          for (e = "@", _++; _ < a;) {
                            if ((o = i.charCodeAt(_)) < 32) m();else if (92 == o) switch (o = i.charCodeAt(++_)) {
                              case 92:
                              case 34:
                              case 47:
                              case 98:
                              case 116:
                              case 110:
                              case 102:
                              case 114:
                                e += C[o], _++;
                                break;

                              case 117:
                                for (t = ++_, n = _ + 4; _ < n; _++) {
                                  48 <= (o = i.charCodeAt(_)) && o <= 57 || 97 <= o && o <= 102 || 65 <= o && o <= 70 || m();
                                }

                                e += b("0x" + i.slice(t, _));
                                break;

                              default:
                                m();
                            } else {
                              if (34 == o) break;

                              for (o = i.charCodeAt(_), t = _; 32 <= o && 92 != o && 34 != o;) {
                                o = i.charCodeAt(++_);
                              }

                              e += i.slice(t, _);
                            }
                          }

                          if (34 == i.charCodeAt(_)) return _++, e;
                          m();

                        default:
                          if (t = _, 45 == o && (r = !0, o = i.charCodeAt(++_)), 48 <= o && o <= 57) {
                            for (48 == o && 48 <= (o = i.charCodeAt(_ + 1)) && o <= 57 && m(), r = !1; _ < a && 48 <= (o = i.charCodeAt(_)) && o <= 57; _++) {
                              ;
                            }

                            if (46 == i.charCodeAt(_)) {
                              for (n = ++_; n < a && 48 <= (o = i.charCodeAt(n)) && o <= 57; n++) {
                                ;
                              }

                              n == _ && m(), _ = n;
                            }

                            if (101 == (o = i.charCodeAt(_)) || 69 == o) {
                              for (43 != (o = i.charCodeAt(++_)) && 45 != o || _++, n = _; n < a && 48 <= (o = i.charCodeAt(n)) && o <= 57; n++) {
                                ;
                              }

                              n == _ && m(), _ = n;
                            }

                            return +i.slice(t, _);
                          }

                          if (r && m(), "true" == i.slice(_, _ + 4)) return _ += 4, !0;
                          if ("false" == i.slice(_, _ + 5)) return _ += 5, !1;
                          if ("null" == i.slice(_, _ + 4)) return _ += 4, null;
                          m();
                      }
                    }

                    return "$";
                  };

                  var y = function y(e, t, n) {
                    var r = U(e, t, n);
                    r === E ? delete e[t] : e[t] = r;
                  };

                  var _,
                      w,
                      b = f.fromCharCode,
                      C = {
                    92: "\\",
                    34: '"',
                    47: "/",
                    98: "\b",
                    116: "\t",
                    110: "\n",
                    102: "\f",
                    114: "\r"
                  },
                      F = function F(e) {
                    var t, n;

                    if ("$" == e && m(), "string" == typeof e) {
                      if ("@" == (s ? e.charAt(0) : e[0])) return e.slice(1);

                      if ("[" == e) {
                        for (t = []; "]" != (e = v()); n = n || !0) {
                          n && ("," == e ? "]" == (e = v()) && m() : m()), "," == e && m(), t.push(F(e));
                        }

                        return t;
                      }

                      if ("{" == e) {
                        for (t = {}; "}" != (e = v()); n = n || !0) {
                          n && ("," == e ? "}" == (e = v()) && m() : m()), "," != e && "string" == typeof e && "@" == (s ? e.charAt(0) : e[0]) && ":" == v() || m(), t[e.slice(1)] = F(v());
                        }

                        return t;
                      }

                      m();
                    }

                    return e;
                  },
                      U = function U(e, t, n) {
                    var r,
                        o = e[t];
                    if ("object" == _typeof(o) && o) if (N.call(o) == M) for (r = o.length; r--;) {
                      y(o, r, n);
                    } else _S(o, function (e) {
                      y(o, e, n);
                    });
                    return n.call(e, t, o);
                  };

                  u.parse = function (e, t) {
                    var n, r;
                    return _ = 0, w = "" + e, n = F(v()), "$" != v() && m(), _ = w = null, t && N.call(t) == g ? U(((r = {})[""] = n, r), "", t) : n;
                  };
                }
              }

              return u.runInContext = $, u;
            }

            var G = {
              function: !0,
              object: !0
            },
                e = G[_typeof(u)] && u && !u.nodeType && u,
                D = G[typeof window === "undefined" ? "undefined" : _typeof(window)] && window || this,
                t = e && G[_typeof(s)] && s && !s.nodeType && "object" == _typeof(a) && a;
            if (!t || t.global !== t && t.window !== t && t.self !== t || (D = t), e) $(D, e);else {
              var n = D.JSON,
                  r = D.JSON3,
                  o = !1,
                  i = $(D, D.JSON3 = {
                noConflict: function noConflict() {
                  return o || (o = !0, D.JSON = n, D.JSON3 = r, n = r = null), i;
                }
              });
              D.JSON = {
                parse: i.parse,
                stringify: i.stringify
              };
            }
          }).call(this);
        }).call(this, "undefined" != typeof global ? global : "undefined" != typeof self ? self : "undefined" != typeof window ? window : {});
      }, {}],
      5: [function (e, t, n) {
        function r() {
          throw new Error("setTimeout has not been defined");
        }

        function o() {
          throw new Error("clearTimeout has not been defined");
        }

        function i(t) {
          if (l === setTimeout) return setTimeout(t, 0);
          if ((l === r || !l) && setTimeout) return l = setTimeout, setTimeout(t, 0);

          try {
            return l(t, 0);
          } catch (e) {
            try {
              return l.call(null, t, 0);
            } catch (e) {
              return l.call(this, t, 0);
            }
          }
        }

        function a() {
          d && h && (d = !1, h.length ? g = h.concat(g) : m = -1, g.length && s());
        }

        function s() {
          if (!d) {
            var e = i(a);
            d = !0;

            for (var t = g.length; t;) {
              for (h = g, g = []; ++m < t;) {
                h && h[m].run();
              }

              m = -1, t = g.length;
            }

            h = null, d = !1, function (t) {
              if (f === clearTimeout) return clearTimeout(t);
              if ((f === o || !f) && clearTimeout) return f = clearTimeout, clearTimeout(t);

              try {
                f(t);
              } catch (e) {
                try {
                  return f.call(null, t);
                } catch (e) {
                  return f.call(this, t);
                }
              }
            }(e);
          }
        }

        function u(e, t) {
          this.fun = e, this.array = t;
        }

        function c() {}

        var l,
            f,
            p = t.exports = {};
        !function () {
          try {
            l = "function" == typeof setTimeout ? setTimeout : r;
          } catch (e) {
            l = r;
          }

          try {
            f = "function" == typeof clearTimeout ? clearTimeout : o;
          } catch (e) {
            f = o;
          }
        }();
        var h,
            g = [],
            d = !1,
            m = -1;
        p.nextTick = function (e) {
          var t = new Array(arguments.length - 1);
          if (1 < arguments.length) for (var n = 1; n < arguments.length; n++) {
            t[n - 1] = arguments[n];
          }
          g.push(new u(e, t)), 1 !== g.length || d || i(s);
        }, u.prototype.run = function () {
          this.fun.apply(null, this.array);
        }, p.title = "browser", p.browser = !0, p.env = {}, p.argv = [], p.version = "", p.versions = {}, p.on = c, p.addListener = c, p.once = c, p.off = c, p.removeListener = c, p.removeAllListeners = c, p.emit = c, p.prependListener = c, p.prependOnceListener = c, p.listeners = function (e) {
          return [];
        }, p.binding = function (e) {
          throw new Error("process.binding is not supported");
        }, p.cwd = function () {
          return "/";
        }, p.chdir = function (e) {
          throw new Error("process.chdir is not supported");
        }, p.umask = function () {
          return 0;
        };
      }, {}],
      6: [function (e, t, n) {
        function i() {
          this._array = [], this._set = Object.create(null);
        }

        var a = e("./util"),
            s = Object.prototype.hasOwnProperty;
        i.fromArray = function (e, t) {
          for (var n = new i(), r = 0, o = e.length; r < o; r++) {
            n.add(e[r], t);
          }

          return n;
        }, i.prototype.size = function () {
          return Object.getOwnPropertyNames(this._set).length;
        }, i.prototype.add = function (e, t) {
          var n = a.toSetString(e),
              r = s.call(this._set, n),
              o = this._array.length;
          r && !t || this._array.push(e), r || (this._set[n] = o);
        }, i.prototype.has = function (e) {
          var t = a.toSetString(e);
          return s.call(this._set, t);
        }, i.prototype.indexOf = function (e) {
          var t = a.toSetString(e);
          if (s.call(this._set, t)) return this._set[t];
          throw new Error('"' + e + '" is not in the set.');
        }, i.prototype.at = function (e) {
          if (0 <= e && e < this._array.length) return this._array[e];
          throw new Error("No element indexed by " + e);
        }, i.prototype.toArray = function () {
          return this._array.slice();
        }, n.ArraySet = i;
      }, {
        "./util": 12
      }],
      7: [function (e, t, n) {
        var u = e("./base64");
        n.encode = function (e) {
          for (var t, n = "", r = function (e) {
            return e < 0 ? 1 + (-e << 1) : 0 + (e << 1);
          }(e); t = 31 & r, 0 < (r >>>= 5) && (t |= 32), n += u.encode(t), 0 < r;) {
            ;
          }

          return n;
        }, n.decode = function (e, t, n) {
          var r,
              o,
              i = e.length,
              a = 0,
              s = 0;

          do {
            if (i <= t) throw new Error("Expected more digits in base 64 VLQ value.");
            if (-1 === (o = u.decode(e.charCodeAt(t++)))) throw new Error("Invalid base64 digit: " + e.charAt(t - 1));
            r = !!(32 & o), a += (o &= 31) << s, s += 5;
          } while (r);

          n.value = function (e) {
            var t = e >> 1;
            return 1 == (1 & e) ? -t : t;
          }(a), n.rest = t;
        };
      }, {
        "./base64": 8
      }],
      8: [function (e, t, n) {
        var r = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".split("");
        n.encode = function (e) {
          if (0 <= e && e < r.length) return r[e];
          throw new TypeError("Must be between 0 and 63: " + e);
        }, n.decode = function (e) {
          return 65 <= e && e <= 90 ? e - 65 : 97 <= e && e <= 122 ? e - 97 + 26 : 48 <= e && e <= 57 ? e - 48 + 52 : 43 == e ? 62 : 47 == e ? 63 : -1;
        };
      }, {}],
      9: [function (e, t, c) {
        c.GREATEST_LOWER_BOUND = 1, c.LEAST_UPPER_BOUND = 2, c.search = function (e, t, n, r) {
          if (0 === t.length) return -1;

          var o = function e(t, n, r, o, i, a) {
            var s = Math.floor((n - t) / 2) + t,
                u = i(r, o[s], !0);
            return 0 === u ? s : 0 < u ? 1 < n - s ? e(s, n, r, o, i, a) : a == c.LEAST_UPPER_BOUND ? n < o.length ? n : -1 : s : 1 < s - t ? e(t, s, r, o, i, a) : a == c.LEAST_UPPER_BOUND ? s : t < 0 ? -1 : t;
          }(-1, t.length, e, t, n, r || c.GREATEST_LOWER_BOUND);

          if (o < 0) return -1;

          for (; 0 <= o - 1 && 0 === n(t[o], t[o - 1], !0);) {
            --o;
          }

          return o;
        };
      }, {}],
      10: [function (e, t, n) {
        function u(e, t, n) {
          var r = e[t];
          e[t] = e[n], e[n] = r;
        }

        function c(e, t, n, r) {
          if (n < r) {
            var o = n - 1;
            u(e, function (e, t) {
              return Math.round(e + Math.random() * (t - e));
            }(n, r), r);

            for (var i = e[r], a = n; a < r; a++) {
              t(e[a], i) <= 0 && u(e, o += 1, a);
            }

            u(e, o + 1, a);
            var s = o + 1;
            c(e, t, n, s - 1), c(e, t, s + 1, r);
          }
        }

        n.quickSort = function (e, t) {
          c(e, t, 0, e.length - 1);
        };
      }, {}],
      11: [function (e, t, n) {
        function a(e) {
          var t = e;
          return "string" == typeof e && (t = JSON.parse(e.replace(/^\)\]\}'/, ""))), null != t.sections ? new r(t) : new f(t);
        }

        function f(e) {
          var t = e;
          "string" == typeof e && (t = JSON.parse(e.replace(/^\)\]\}'/, "")));
          var n = w.getArg(t, "version"),
              r = w.getArg(t, "sources"),
              o = w.getArg(t, "names", []),
              i = w.getArg(t, "sourceRoot", null),
              a = w.getArg(t, "sourcesContent", null),
              s = w.getArg(t, "mappings"),
              u = w.getArg(t, "file", null);
          if (n != this._version) throw new Error("Unsupported version: " + n);
          r = r.map(String).map(w.normalize).map(function (e) {
            return i && w.isAbsolute(i) && w.isAbsolute(e) ? w.relative(i, e) : e;
          }), this._names = p.fromArray(o.map(String), !0), this._sources = p.fromArray(r, !0), this.sourceRoot = i, this.sourcesContent = a, this._mappings = s, this.file = u;
        }

        function _() {
          this.generatedLine = 0, this.generatedColumn = 0, this.source = null, this.originalLine = null, this.originalColumn = null, this.name = null;
        }

        function r(e) {
          var t = e;
          "string" == typeof e && (t = JSON.parse(e.replace(/^\)\]\}'/, "")));
          var n = w.getArg(t, "version"),
              r = w.getArg(t, "sections");
          if (n != this._version) throw new Error("Unsupported version: " + n);
          this._sources = new p(), this._names = new p();
          var o = {
            line: -1,
            column: 0
          };
          this._sections = r.map(function (e) {
            if (e.url) throw new Error("Support for url field in sections not implemented.");
            var t = w.getArg(e, "offset"),
                n = w.getArg(t, "line"),
                r = w.getArg(t, "column");
            if (n < o.line || n === o.line && r < o.column) throw new Error("Section offsets must be ordered and non-overlapping.");
            return o = t, {
              generatedOffset: {
                generatedLine: n + 1,
                generatedColumn: r + 1
              },
              consumer: new a(w.getArg(e, "map"))
            };
          });
        }

        var w = e("./util"),
            u = e("./binary-search"),
            p = e("./array-set").ArraySet,
            b = e("./base64-vlq"),
            C = e("./quick-sort").quickSort;
        a.fromSourceMap = function (e) {
          return f.fromSourceMap(e);
        }, a.prototype._version = 3, a.prototype.__generatedMappings = null, Object.defineProperty(a.prototype, "_generatedMappings", {
          get: function get() {
            return this.__generatedMappings || this._parseMappings(this._mappings, this.sourceRoot), this.__generatedMappings;
          }
        }), a.prototype.__originalMappings = null, Object.defineProperty(a.prototype, "_originalMappings", {
          get: function get() {
            return this.__originalMappings || this._parseMappings(this._mappings, this.sourceRoot), this.__originalMappings;
          }
        }), a.prototype._charIsMappingSeparator = function (e, t) {
          var n = e.charAt(t);
          return ";" === n || "," === n;
        }, a.prototype._parseMappings = function (e, t) {
          throw new Error("Subclasses must implement _parseMappings");
        }, a.GENERATED_ORDER = 1, a.ORIGINAL_ORDER = 2, a.GREATEST_LOWER_BOUND = 1, a.LEAST_UPPER_BOUND = 2, a.prototype.eachMapping = function (e, t, n) {
          var r,
              o = t || null;

          switch (n || a.GENERATED_ORDER) {
            case a.GENERATED_ORDER:
              r = this._generatedMappings;
              break;

            case a.ORIGINAL_ORDER:
              r = this._originalMappings;
              break;

            default:
              throw new Error("Unknown order of iteration.");
          }

          var i = this.sourceRoot;
          r.map(function (e) {
            var t = null === e.source ? null : this._sources.at(e.source);
            return null != t && null != i && (t = w.join(i, t)), {
              source: t,
              generatedLine: e.generatedLine,
              generatedColumn: e.generatedColumn,
              originalLine: e.originalLine,
              originalColumn: e.originalColumn,
              name: null === e.name ? null : this._names.at(e.name)
            };
          }, this).forEach(e, o);
        }, a.prototype.allGeneratedPositionsFor = function (e) {
          var t = w.getArg(e, "line"),
              n = {
            source: w.getArg(e, "source"),
            originalLine: t,
            originalColumn: w.getArg(e, "column", 0)
          };
          if (null != this.sourceRoot && (n.source = w.relative(this.sourceRoot, n.source)), !this._sources.has(n.source)) return [];
          n.source = this._sources.indexOf(n.source);

          var r = [],
              o = this._findMapping(n, this._originalMappings, "originalLine", "originalColumn", w.compareByOriginalPositions, u.LEAST_UPPER_BOUND);

          if (0 <= o) {
            var i = this._originalMappings[o];
            if (void 0 === e.column) for (var a = i.originalLine; i && i.originalLine === a;) {
              r.push({
                line: w.getArg(i, "generatedLine", null),
                column: w.getArg(i, "generatedColumn", null),
                lastColumn: w.getArg(i, "lastGeneratedColumn", null)
              }), i = this._originalMappings[++o];
            } else for (var s = i.originalColumn; i && i.originalLine === t && i.originalColumn == s;) {
              r.push({
                line: w.getArg(i, "generatedLine", null),
                column: w.getArg(i, "generatedColumn", null),
                lastColumn: w.getArg(i, "lastGeneratedColumn", null)
              }), i = this._originalMappings[++o];
            }
          }

          return r;
        }, n.SourceMapConsumer = a, (f.prototype = Object.create(a.prototype)).consumer = a, f.fromSourceMap = function (e) {
          var t = Object.create(f.prototype),
              n = t._names = p.fromArray(e._names.toArray(), !0),
              r = t._sources = p.fromArray(e._sources.toArray(), !0);
          t.sourceRoot = e._sourceRoot, t.sourcesContent = e._generateSourcesContent(t._sources.toArray(), t.sourceRoot), t.file = e._file;

          for (var o = e._mappings.toArray().slice(), i = t.__generatedMappings = [], a = t.__originalMappings = [], s = 0, u = o.length; s < u; s++) {
            var c = o[s],
                l = new _();
            l.generatedLine = c.generatedLine, l.generatedColumn = c.generatedColumn, c.source && (l.source = r.indexOf(c.source), l.originalLine = c.originalLine, l.originalColumn = c.originalColumn, c.name && (l.name = n.indexOf(c.name)), a.push(l)), i.push(l);
          }

          return C(t.__originalMappings, w.compareByOriginalPositions), t;
        }, f.prototype._version = 3, Object.defineProperty(f.prototype, "sources", {
          get: function get() {
            return this._sources.toArray().map(function (e) {
              return null != this.sourceRoot ? w.join(this.sourceRoot, e) : e;
            }, this);
          }
        }), f.prototype._parseMappings = function (e, t) {
          for (var n, r, o, i, a, s = 1, u = 0, c = 0, l = 0, f = 0, p = 0, h = e.length, g = 0, d = {}, m = {}, v = [], y = []; g < h;) {
            if (";" === e.charAt(g)) s++, g++, u = 0;else if ("," === e.charAt(g)) g++;else {
              for ((n = new _()).generatedLine = s, i = g; i < h && !this._charIsMappingSeparator(e, i); i++) {
                ;
              }

              if (o = d[r = e.slice(g, i)]) g += r.length;else {
                for (o = []; g < i;) {
                  b.decode(e, g, m), a = m.value, g = m.rest, o.push(a);
                }

                if (2 === o.length) throw new Error("Found a source, but no line and column");
                if (3 === o.length) throw new Error("Found a source and line, but no column");
                d[r] = o;
              }
              n.generatedColumn = u + o[0], u = n.generatedColumn, 1 < o.length && (n.source = f + o[1], f += o[1], n.originalLine = c + o[2], c = n.originalLine, n.originalLine += 1, n.originalColumn = l + o[3], l = n.originalColumn, 4 < o.length && (n.name = p + o[4], p += o[4])), y.push(n), "number" == typeof n.originalLine && v.push(n);
            }
          }

          C(y, w.compareByGeneratedPositionsDeflated), this.__generatedMappings = y, C(v, w.compareByOriginalPositions), this.__originalMappings = v;
        }, f.prototype._findMapping = function (e, t, n, r, o, i) {
          if (e[n] <= 0) throw new TypeError("Line must be greater than or equal to 1, got " + e[n]);
          if (e[r] < 0) throw new TypeError("Column must be greater than or equal to 0, got " + e[r]);
          return u.search(e, t, o, i);
        }, f.prototype.computeColumnSpans = function () {
          for (var e = 0; e < this._generatedMappings.length; ++e) {
            var t = this._generatedMappings[e];

            if (e + 1 < this._generatedMappings.length) {
              var n = this._generatedMappings[e + 1];

              if (t.generatedLine === n.generatedLine) {
                t.lastGeneratedColumn = n.generatedColumn - 1;
                continue;
              }
            }

            t.lastGeneratedColumn = 1 / 0;
          }
        }, f.prototype.originalPositionFor = function (e) {
          var t = {
            generatedLine: w.getArg(e, "line"),
            generatedColumn: w.getArg(e, "column")
          },
              n = this._findMapping(t, this._generatedMappings, "generatedLine", "generatedColumn", w.compareByGeneratedPositionsDeflated, w.getArg(e, "bias", a.GREATEST_LOWER_BOUND));

          if (0 <= n) {
            var r = this._generatedMappings[n];

            if (r.generatedLine === t.generatedLine) {
              var o = w.getArg(r, "source", null);
              null !== o && (o = this._sources.at(o), null != this.sourceRoot && (o = w.join(this.sourceRoot, o)));
              var i = w.getArg(r, "name", null);
              return null !== i && (i = this._names.at(i)), {
                source: o,
                line: w.getArg(r, "originalLine", null),
                column: w.getArg(r, "originalColumn", null),
                name: i
              };
            }
          }

          return {
            source: null,
            line: null,
            column: null,
            name: null
          };
        }, f.prototype.hasContentsOfAllSources = function () {
          return !!this.sourcesContent && this.sourcesContent.length >= this._sources.size() && !this.sourcesContent.some(function (e) {
            return null == e;
          });
        }, f.prototype.sourceContentFor = function (e, t) {
          if (!this.sourcesContent) return null;
          if (null != this.sourceRoot && (e = w.relative(this.sourceRoot, e)), this._sources.has(e)) return this.sourcesContent[this._sources.indexOf(e)];
          var n;

          if (null != this.sourceRoot && (n = w.urlParse(this.sourceRoot))) {
            var r = e.replace(/^file:\/\//, "");
            if ("file" == n.scheme && this._sources.has(r)) return this.sourcesContent[this._sources.indexOf(r)];
            if ((!n.path || "/" == n.path) && this._sources.has("/" + e)) return this.sourcesContent[this._sources.indexOf("/" + e)];
          }

          if (t) return null;
          throw new Error('"' + e + '" is not in the SourceMap.');
        }, f.prototype.generatedPositionFor = function (e) {
          var t = w.getArg(e, "source");
          if (null != this.sourceRoot && (t = w.relative(this.sourceRoot, t)), !this._sources.has(t)) return {
            line: null,
            column: null,
            lastColumn: null
          };

          var n = {
            source: t = this._sources.indexOf(t),
            originalLine: w.getArg(e, "line"),
            originalColumn: w.getArg(e, "column")
          },
              r = this._findMapping(n, this._originalMappings, "originalLine", "originalColumn", w.compareByOriginalPositions, w.getArg(e, "bias", a.GREATEST_LOWER_BOUND));

          if (0 <= r) {
            var o = this._originalMappings[r];
            if (o.source === n.source) return {
              line: w.getArg(o, "generatedLine", null),
              column: w.getArg(o, "generatedColumn", null),
              lastColumn: w.getArg(o, "lastGeneratedColumn", null)
            };
          }

          return {
            line: null,
            column: null,
            lastColumn: null
          };
        }, n.BasicSourceMapConsumer = f, (r.prototype = Object.create(a.prototype)).constructor = a, r.prototype._version = 3, Object.defineProperty(r.prototype, "sources", {
          get: function get() {
            for (var e = [], t = 0; t < this._sections.length; t++) {
              for (var n = 0; n < this._sections[t].consumer.sources.length; n++) {
                e.push(this._sections[t].consumer.sources[n]);
              }
            }

            return e;
          }
        }), r.prototype.originalPositionFor = function (e) {
          var t = {
            generatedLine: w.getArg(e, "line"),
            generatedColumn: w.getArg(e, "column")
          },
              n = u.search(t, this._sections, function (e, t) {
            var n = e.generatedLine - t.generatedOffset.generatedLine;
            return n || e.generatedColumn - t.generatedOffset.generatedColumn;
          }),
              r = this._sections[n];
          return r ? r.consumer.originalPositionFor({
            line: t.generatedLine - (r.generatedOffset.generatedLine - 1),
            column: t.generatedColumn - (r.generatedOffset.generatedLine === t.generatedLine ? r.generatedOffset.generatedColumn - 1 : 0),
            bias: e.bias
          }) : {
            source: null,
            line: null,
            column: null,
            name: null
          };
        }, r.prototype.hasContentsOfAllSources = function () {
          return this._sections.every(function (e) {
            return e.consumer.hasContentsOfAllSources();
          });
        }, r.prototype.sourceContentFor = function (e, t) {
          for (var n = 0; n < this._sections.length; n++) {
            var r = this._sections[n].consumer.sourceContentFor(e, !0);

            if (r) return r;
          }

          if (t) return null;
          throw new Error('"' + e + '" is not in the SourceMap.');
        }, r.prototype.generatedPositionFor = function (e) {
          for (var t = 0; t < this._sections.length; t++) {
            var n = this._sections[t];

            if (-1 !== n.consumer.sources.indexOf(w.getArg(e, "source"))) {
              var r = n.consumer.generatedPositionFor(e);
              if (r) return {
                line: r.line + (n.generatedOffset.generatedLine - 1),
                column: r.column + (n.generatedOffset.generatedLine === r.line ? n.generatedOffset.generatedColumn - 1 : 0)
              };
            }
          }

          return {
            line: null,
            column: null
          };
        }, r.prototype._parseMappings = function (e, t) {
          this.__generatedMappings = [], this.__originalMappings = [];

          for (var n = 0; n < this._sections.length; n++) {
            for (var r = this._sections[n], o = r.consumer._generatedMappings, i = 0; i < o.length; i++) {
              var a = o[i],
                  s = r.consumer._sources.at(a.source);

              null !== r.consumer.sourceRoot && (s = w.join(r.consumer.sourceRoot, s)), this._sources.add(s), s = this._sources.indexOf(s);

              var u = r.consumer._names.at(a.name);

              this._names.add(u), u = this._names.indexOf(u);
              var c = {
                source: s,
                generatedLine: a.generatedLine + (r.generatedOffset.generatedLine - 1),
                generatedColumn: a.generatedColumn + (r.generatedOffset.generatedLine === a.generatedLine ? r.generatedOffset.generatedColumn - 1 : 0),
                originalLine: a.originalLine,
                originalColumn: a.originalColumn,
                name: u
              };
              this.__generatedMappings.push(c), "number" == typeof c.originalLine && this.__originalMappings.push(c);
            }
          }

          C(this.__generatedMappings, w.compareByGeneratedPositionsDeflated), C(this.__originalMappings, w.compareByOriginalPositions);
        }, n.IndexedSourceMapConsumer = r;
      }, {
        "./array-set": 6,
        "./base64-vlq": 7,
        "./binary-search": 9,
        "./quick-sort": 10,
        "./util": 12
      }],
      12: [function (e, t, u) {
        function c(e) {
          var t = e.match(a);
          return t ? {
            scheme: t[1],
            auth: t[2],
            host: t[3],
            port: t[4],
            path: t[5]
          } : null;
        }

        function l(e) {
          var t = "";
          return e.scheme && (t += e.scheme + ":"), t += "//", e.auth && (t += e.auth + "@"), e.host && (t += e.host), e.port && (t += ":" + e.port), e.path && (t += e.path), t;
        }

        function i(e) {
          var t = e,
              n = c(e);

          if (n) {
            if (!n.path) return e;
            t = n.path;
          }

          for (var r, o = u.isAbsolute(t), i = t.split(/\/+/), a = 0, s = i.length - 1; 0 <= s; s--) {
            "." === (r = i[s]) ? i.splice(s, 1) : ".." === r ? a++ : 0 < a && ("" === r ? (i.splice(s + 1, a), a = 0) : (i.splice(s, 2), a--));
          }

          return "" === (t = i.join("/")) && (t = o ? "/" : "."), n ? (n.path = t, l(n)) : t;
        }

        function n(e) {
          return e;
        }

        function r(e) {
          if (!e) return !1;
          var t = e.length;
          if (t < 9) return !1;
          if (95 !== e.charCodeAt(t - 1) || 95 !== e.charCodeAt(t - 2) || 111 !== e.charCodeAt(t - 3) || 116 !== e.charCodeAt(t - 4) || 111 !== e.charCodeAt(t - 5) || 114 !== e.charCodeAt(t - 6) || 112 !== e.charCodeAt(t - 7) || 95 !== e.charCodeAt(t - 8) || 95 !== e.charCodeAt(t - 9)) return !1;

          for (var n = t - 10; 0 <= n; n--) {
            if (36 !== e.charCodeAt(n)) return !1;
          }

          return !0;
        }

        function o(e, t) {
          return e === t ? 0 : t < e ? 1 : -1;
        }

        u.getArg = function (e, t, n) {
          if (t in e) return e[t];
          if (3 === arguments.length) return n;
          throw new Error('"' + t + '" is a required argument.');
        };

        var a = /^(?:([\w+\-.]+):)?\/\/(?:(\w+:\w+)@)?([\w.]*)(?::(\d+))?(\S*)$/,
            s = /^data:.+\,.+$/;
        u.urlParse = c, u.urlGenerate = l, u.normalize = i, u.join = function (e, t) {
          "" === e && (e = "."), "" === t && (t = ".");
          var n = c(t),
              r = c(e);
          if (r && (e = r.path || "/"), n && !n.scheme) return r && (n.scheme = r.scheme), l(n);
          if (n || t.match(s)) return t;
          if (r && !r.host && !r.path) return r.host = t, l(r);
          var o = "/" === t.charAt(0) ? t : i(e.replace(/\/+$/, "") + "/" + t);
          return r ? (r.path = o, l(r)) : o;
        }, u.isAbsolute = function (e) {
          return "/" === e.charAt(0) || !!e.match(a);
        }, u.relative = function (e, t) {
          "" === e && (e = "."), e = e.replace(/\/$/, "");

          for (var n = 0; 0 !== t.indexOf(e + "/");) {
            var r = e.lastIndexOf("/");
            if (r < 0) return t;
            if ((e = e.slice(0, r)).match(/^([^\/]+:\/)?\/*$/)) return t;
            ++n;
          }

          return Array(n + 1).join("../") + t.substr(e.length + 1);
        };
        var f = !("__proto__" in Object.create(null));
        u.toSetString = f ? n : function (e) {
          return r(e) ? "$" + e : e;
        }, u.fromSetString = f ? n : function (e) {
          return r(e) ? e.slice(1) : e;
        }, u.compareByOriginalPositions = function (e, t, n) {
          var r = e.source - t.source;
          return 0 !== r ? r : 0 !== (r = e.originalLine - t.originalLine) ? r : 0 !== (r = e.originalColumn - t.originalColumn) || n ? r : 0 !== (r = e.generatedColumn - t.generatedColumn) ? r : 0 !== (r = e.generatedLine - t.generatedLine) ? r : e.name - t.name;
        }, u.compareByGeneratedPositionsDeflated = function (e, t, n) {
          var r = e.generatedLine - t.generatedLine;
          return 0 !== r ? r : 0 !== (r = e.generatedColumn - t.generatedColumn) || n ? r : 0 !== (r = e.source - t.source) ? r : 0 !== (r = e.originalLine - t.originalLine) ? r : 0 !== (r = e.originalColumn - t.originalColumn) ? r : e.name - t.name;
        }, u.compareByGeneratedPositionsInflated = function (e, t) {
          var n = e.generatedLine - t.generatedLine;
          return 0 !== n ? n : 0 !== (n = e.generatedColumn - t.generatedColumn) ? n : 0 !== (n = o(e.source, t.source)) ? n : 0 !== (n = e.originalLine - t.originalLine) ? n : 0 !== (n = e.originalColumn - t.originalColumn) ? n : o(e.name, t.name);
        };
      }, {}],
      13: [function (e, t, n) {
        arguments[4][2][0].apply(n, arguments);
      }, {
        dup: 2
      }],
      14: [function (n, r, o) {
        !function (e, t) {
          "use strict";

          "object" == _typeof(o) ? r.exports = t(n("stackframe")) : e.StackGenerator = t(e.StackFrame);
        }(this, function (a) {
          return {
            backtrace: function backtrace(e) {
              var t = [],
                  n = 10;
              "object" == _typeof(e) && "number" == typeof e.maxStackSize && (n = e.maxStackSize);

              for (var r = arguments.callee; r && t.length < n && r.arguments;) {
                for (var o = new Array(r.arguments.length), i = 0; i < o.length; ++i) {
                  o[i] = r.arguments[i];
                }

                /function(?:\s+([\w$]+))+\s*\(/.test(r.toString()) ? t.push(new a({
                  functionName: RegExp.$1 || void 0,
                  args: o
                })) : t.push(new a({
                  args: o
                }));

                try {
                  r = r.caller;
                } catch (e) {
                  break;
                }
              }

              return t;
            }
          };
        });
      }, {
        stackframe: 13
      }],
      15: [function (e, t, n) {
        arguments[4][2][0].apply(n, arguments);
      }, {
        dup: 2
      }],
      16: [function (n, r, o) {
        !function (e, t) {
          "use strict";

          "object" == _typeof(o) ? r.exports = t(n("source-map/lib/source-map-consumer"), n("stackframe")) : e.StackTraceGPS = t(e.SourceMap || e.sourceMap, e.StackFrame);
        }(this, function (i, u) {
          "use strict";

          function t(r) {
            return new Promise(function (e, t) {
              var n = new XMLHttpRequest();
              n.open("get", r), n.onerror = t, n.onreadystatechange = function () {
                4 === n.readyState && (200 <= n.status && n.status < 300 || "file://" === r.substr(0, 7) && n.responseText ? e(n.responseText) : t(new Error("HTTP status: " + n.status + " retrieving " + r)));
              }, n.send();
            });
          }

          function n(e) {
            if ("undefined" != typeof window && window.atob) return window.atob(e);
            throw new Error("You must supply a polyfill for window.atob in this environment");
          }

          function r(e) {
            if ("object" != _typeof(e)) throw new TypeError("Given StackFrame is not an object");
            if ("string" != typeof e.fileName) throw new TypeError("Given file name is not a String");
            if ("number" != typeof e.lineNumber || e.lineNumber % 1 != 0 || e.lineNumber < 1) throw new TypeError("Given line number must be a positive integer");
            if ("number" != typeof e.columnNumber || e.columnNumber % 1 != 0 || e.columnNumber < 0) throw new TypeError("Given column number must be a non-negative integer");
            return !0;
          }

          return function e(c) {
            return _instanceof(this, e) ? (c = c || {}, this.sourceCache = c.sourceCache || {}, this.sourceMapConsumerCache = c.sourceMapConsumerCache || {}, this.ajax = c.ajax || t, this._atob = c.atob || n, this._get = function (u) {
              return new Promise(function (e, t) {
                var n = "data:" === u.substr(0, 5);
                if (this.sourceCache[u]) e(this.sourceCache[u]);else if (c.offline && !n) t(new Error("Cannot make network requests in offline mode"));else if (n) {
                  var r = u.match(/^data:application\/json;([\w=:"-]+;)*base64,/);

                  if (r) {
                    var o = r[0].length,
                        i = u.substr(o),
                        a = this._atob(i);

                    e(this.sourceCache[u] = a);
                  } else t(new Error("The encoding of the inline sourcemap is not supported"));
                } else {
                  var s = this.ajax(u, {
                    method: "get"
                  });
                  (this.sourceCache[u] = s).then(e, t);
                }
              }.bind(this));
            }, this._getSourceMapConsumer = function (r, o) {
              return new Promise(function (e, t) {
                if (this.sourceMapConsumerCache[r]) e(this.sourceMapConsumerCache[r]);else {
                  var n = new Promise(function (t, e) {
                    return this._get(r).then(function (e) {
                      "string" == typeof e && (e = function (e) {
                        if ("undefined" != typeof JSON && JSON.parse) return JSON.parse(e);
                        throw new Error("You must supply a polyfill for JSON.parse in this environment");
                      }(e.replace(/^\)\]\}'/, ""))), void 0 === e.sourceRoot && (e.sourceRoot = o), t(new i.SourceMapConsumer(e));
                    }, e);
                  }.bind(this));
                  e(this.sourceMapConsumerCache[r] = n);
                }
              }.bind(this));
            }, this.pinpoint = function (t) {
              return new Promise(function (n, e) {
                this.getMappedLocation(t).then(function (e) {
                  function t() {
                    n(e);
                  }

                  this.findFunctionName(e).then(n, t).catch(t);
                }.bind(this), e);
              }.bind(this));
            }, this.findFunctionName = function (i) {
              return new Promise(function (o, e) {
                r(i), this._get(i.fileName).then(function (e) {
                  var t = i.lineNumber,
                      n = i.columnNumber,
                      r = function (e, t) {
                    for (var n = [/['"]?([$_A-Za-z][$_A-Za-z0-9]*)['"]?\s*[:=]\s*function\b/, /function\s+([^('"`]*?)\s*\(([^)]*)\)/, /['"]?([$_A-Za-z][$_A-Za-z0-9]*)['"]?\s*[:=]\s*(?:eval|new Function)\b/, /\b(?!(?:if|for|switch|while|with|catch)\b)(?:(?:static)\s+)?(\S+)\s*\(.*?\)\s*\{/, /['"]?([$_A-Za-z][$_A-Za-z0-9]*)['"]?\s*[:=]\s*\(.*?\)\s*=>/], r = e.split("\n"), o = "", i = Math.min(t, 20), a = 0; a < i; ++a) {
                      var s = r[t - a - 1],
                          u = s.indexOf("//");

                      if (0 <= u && (s = s.substr(0, u)), s) {
                        o = s + o;

                        for (var c = n.length, l = 0; l < c; l++) {
                          var f = n[l].exec(o);
                          if (f && f[1]) return f[1];
                        }
                      }
                    }
                  }(e, t);

                  o(r ? new u({
                    functionName: r,
                    args: i.args,
                    fileName: i.fileName,
                    lineNumber: t,
                    columnNumber: n
                  }) : i);
                }, e).catch(e);
              }.bind(this));
            }, void (this.getMappedLocation = function (s) {
              return new Promise(function (o, e) {
                (function () {
                  if ("function" != typeof Object.defineProperty || "function" != typeof Object.create) throw new Error("Unable to consume source maps in older browsers");
                })(), r(s);
                var i = this.sourceCache,
                    a = s.fileName;

                this._get(a).then(function (e) {
                  var t = function (e) {
                    for (var t, n, r = /\/\/[#@] ?sourceMappingURL=([^\s'"]+)\s*$/gm; n = r.exec(e);) {
                      t = n[1];
                    }

                    if (t) return t;
                    throw new Error("sourceMappingURL not found");
                  }(e),
                      n = "data:" === t.substr(0, 5),
                      r = a.substring(0, a.lastIndexOf("/") + 1);

                  return "/" === t[0] || n || /^https?:\/\/|^\/\//i.test(t) || (t = r + t), this._getSourceMapConsumer(t, r).then(function (e) {
                    return function (o, i, a) {
                      return new Promise(function (e, t) {
                        var n = i.originalPositionFor({
                          line: o.lineNumber,
                          column: o.columnNumber
                        });

                        if (n.source) {
                          var r = i.sourceContentFor(n.source);
                          r && (a[n.source] = r), e(new u({
                            functionName: n.name || o.functionName,
                            args: o.args,
                            fileName: n.source,
                            lineNumber: n.line,
                            columnNumber: n.column
                          }));
                        } else t(new Error("Could not get original source for given stackframe and source map"));
                      });
                    }(s, e, i).then(o).catch(function () {
                      o(s);
                    });
                  });
                }.bind(this), e).catch(e);
              }.bind(this));
            })) : new e(c);
          };
        });
      }, {
        "source-map/lib/source-map-consumer": 11,
        stackframe: 15
      }],
      17: [function (e, t, n) {
        Array.isArray || (Array.isArray = function (e) {
          return "[object Array]" === Object.prototype.toString.call(e);
        }), "undefined" == typeof Promise && ES6Promise.polyfill(), Function.prototype.bind || (Function.prototype.bind = function (e) {
          if ("function" != typeof this) throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");

          function t() {}

          function n() {
            return o.apply(_instanceof(this, t) && e ? this : e, r.concat(Array.prototype.slice.call(arguments)));
          }

          var r = Array.prototype.slice.call(arguments, 1),
              o = this;
          return t.prototype = this.prototype, n.prototype = new t(), n;
        }), Array.prototype.map || (Array.prototype.map = function (e, t) {
          if (null == this) throw new TypeError("this is null or not defined");
          var n,
              r = Object(this),
              o = r.length >>> 0;
          if ("function" != typeof e) throw new TypeError(e + " is not a function");
          1 < arguments.length && (n = t);

          for (var i = new Array(o), a = 0; a < o;) {
            var s, u;
            a in r && (s = r[a], u = e.call(n, s, a, r), i[a] = u), a++;
          }

          return i;
        }), Array.prototype.filter || (Array.prototype.filter = function (e) {
          if (null == this) throw new TypeError("this is null or not defined");
          var t = Object(this),
              n = t.length >>> 0;
          if ("function" != typeof e) throw new TypeError(e + " is not a function");

          for (var r = [], o = 2 <= arguments.length ? arguments[1] : void 0, i = 0; i < n; i++) {
            if (i in t) {
              var a = t[i];
              e.call(o, a, i, t) && r.push(a);
            }
          }

          return r;
        }), Array.prototype.forEach || (Array.prototype.forEach = function (e, t) {
          var n, r;
          if (null == this) throw new TypeError(" this is null or not defined");
          var o = Object(this),
              i = o.length >>> 0;
          if ("function" != typeof e) throw new TypeError(e + " is not a function");

          for (1 < arguments.length && (n = t), r = 0; r < i;) {
            var a;
            r in o && (a = o[r], e.call(n, a, r, o)), r++;
          }
        });
      }, {}],
      18: [function (n, r, o) {
        !function (e, t) {
          "use strict";

          "object" == _typeof(o) ? r.exports = t(n("error-stack-parser"), n("stack-generator"), n("stacktrace-gps")) : e.StackTrace = t(e.ErrorStackParser, e.StackGenerator, e.StackTraceGPS);
        }(this, function (i, n, e) {
          function a(e, t) {
            var n = {};
            return [e, t].forEach(function (e) {
              for (var t in e) {
                e.hasOwnProperty(t) && (n[t] = e[t]);
              }

              return n;
            }), n;
          }

          function s(e) {
            return e.stack || e["opera#sourceloc"];
          }

          function u(e, t) {
            return "function" == typeof t ? e.filter(t) : e;
          }

          function r() {
            try {
              throw new Error();
            } catch (e) {
              return e;
            }
          }

          var c = {
            filter: function filter(e) {
              return -1 === (e.functionName || "").indexOf("StackTrace$$") && -1 === (e.functionName || "").indexOf("ErrorStackParser$$") && -1 === (e.functionName || "").indexOf("StackTraceGPS$$") && -1 === (e.functionName || "").indexOf("StackGenerator$$");
            },
            sourceCache: {}
          };
          return {
            get: function get(e) {
              var t = r();
              return s(t) ? this.fromError(t, e) : this.generateArtificially(e);
            },
            getSync: function getSync(e) {
              e = a(c, e);
              var t = r();
              return u(s(t) ? i.parse(t) : n.backtrace(e), e.filter);
            },
            fromError: function fromError(n, r) {
              r = a(c, r);
              var o = new e(r);
              return new Promise(function (e) {
                var t = u(i.parse(n), r.filter);
                e(Promise.all(t.map(function (n) {
                  return new Promise(function (e) {
                    function t() {
                      e(n);
                    }

                    o.pinpoint(n).then(e, t).catch(t);
                  });
                })));
              }.bind(this));
            },
            generateArtificially: function generateArtificially(e) {
              e = a(c, e);
              var t = n.backtrace(e);
              return "function" == typeof e.filter && (t = t.filter(e.filter)), Promise.resolve(t);
            },
            instrument: function instrument(e, t, n, r) {
              if ("function" != typeof e) throw new Error("Cannot instrument non-function object");
              if ("function" == typeof e.__stacktraceOriginalFn) return e;

              var o = function () {
                try {
                  return this.get().then(t, n).catch(n), e.apply(r || this, arguments);
                } catch (e) {
                  throw s(e) && this.fromError(e).then(t, n).catch(n), e;
                }
              }.bind(this);

              return o.__stacktraceOriginalFn = e, o;
            },
            deinstrument: function deinstrument(e) {
              if ("function" != typeof e) throw new Error("Cannot de-instrument non-function object");
              return "function" == typeof e.__stacktraceOriginalFn ? e.__stacktraceOriginalFn : e;
            },
            report: function report(a, s, u, c) {
              return new Promise(function (e, t) {
                var n = new XMLHttpRequest();

                if (n.onerror = t, n.onreadystatechange = function () {
                  4 === n.readyState && (200 <= n.status && n.status < 400 ? e(n.responseText) : t(new Error("POST to " + s + " failed with status: " + n.status)));
                }, n.open("post", s), n.setRequestHeader("Content-Type", "application/json"), c && "object" == _typeof(c.headers)) {
                  var r = c.headers;

                  for (var o in r) {
                    r.hasOwnProperty(o) && n.setRequestHeader(o, r[o]);
                  }
                }

                var i = {
                  stack: a
                };
                null != u && (i.message = u), n.send(JSON.stringify(i));
              });
            }
          };
        });
      }, {
        "error-stack-parser": 1,
        "stack-generator": 14,
        "stacktrace-gps": 16
      }]
    }, {}, [3, 4, 17, 18])(18);
  });

  function decycle(object, replacer) {
    "use strict";

    var objects = new WeakMap(); // object to path mappings

    return function derez(value, path) {
      // The derez function recurses through the object, producing the deep copy.
      var old_path; // The path of an earlier occurance of value

      var nu; // The new object or array
      // If a replacer function was provided, then call it to get a replacement value.

      if (replacer !== undefined) {
        value = replacer(value);
      } // typeof null === "object", so go on if this value is really an object but not
      // one of the weird builtin objects.


      if (_typeof(value) === "object" && value !== null && !_instanceof(value, Boolean) && !_instanceof(value, Date) && !_instanceof(value, Number) && !_instanceof(value, RegExp) && !_instanceof(value, String)) {
        // If the value is an object or array, look to see if we have already
        // encountered it. If so, return a {"$ref":PATH} object. This uses an
        // ES6 WeakMap.
        old_path = objects.get(value);

        if (old_path !== undefined) {
          return {
            $ref: old_path
          };
        } // Otherwise, accumulate the unique value and its path.


        objects.set(value, path); // If it is an array, replicate the array.

        if (Array.isArray(value)) {
          nu = [];
          value.forEach(function (element, i) {
            nu[i] = derez(element, path + "[" + i + "]");
          });
        } else if (_instanceof(value, Map)) {
          // If it is a Map, replicate the object.
          nu = new Map();

          var _iterator = _createForOfIteratorHelper(value.entries()),
              _step;

          try {
            for (_iterator.s(); !(_step = _iterator.n()).done;) {
              var _step$value = _slicedToArray(_step.value, 2),
                  k = _step$value[0],
                  v = _step$value[1];

              nu.set(derez(k, path + "[Key:" + JSON.stringify(k) + "]"), // Caution! The original retrocycle will not work if the cycle appears in key.
              derez(v, path + "[" + JSON.stringify(k) + "]"));
            }
          } catch (err) {
            _iterator.e(err);
          } finally {
            _iterator.f();
          }
        } else if (_instanceof(value, Set)) {
          // If it is a Set, replicate the object.
          nu = new Set();

          var _iterator2 = _createForOfIteratorHelper(value.keys()),
              _step2;

          try {
            for (_iterator2.s(); !(_step2 = _iterator2.n()).done;) {
              var _v = _step2.value;
              nu.add(derez(_v, path + "[" + JSON.stringify(_v) + "]"));
            }
          } catch (err) {
            _iterator2.e(err);
          } finally {
            _iterator2.f();
          }
        } else {
          // If it is an object, replicate the object.
          nu = {};
          Object.keys(value).forEach(function (name) {
            nu[name] = derez(value[name], path + "[" + JSON.stringify(name) + "]");
          });
        }

        return nu;
      }

      return value;
    }(object, "$");
  }

  ;

  function jsonDecycleAndStringify(obj) {
    // TODO: decycle will replace object's properties
    // the replace function in stringify is of little use
    return JSON.stringify(decycle(obj), function (_, v) {
      if (v === null) return null;
      if (typeof v === 'undefined' || typeof v.constructor === 'undefined') return '__undefined__'; // try our best to get a meaningful name

      var n = v.constructor.name; // Primitive types

      if (n === "Boolean" || n === "String" || n === "Number" || n === "Array" || n === "Object") return v; // Implement toJSON

      if (typeof v.toJSON !== 'undefined') return v; // TypedArray: https://stackoverflow.com/a/29651223

      if (ArrayBuffer.isView(v) && !_instanceof(v, DataView)) return v; // Special case for Map
      // https://stackoverflow.com/a/56150320 

      if (n === "Map") {
        return {
          specialReplacerForES6DataStructure_dataType: 'Map',
          specialReplacerForES6DataStructure_value: Array.from(v.entries())
        };
      } // Special case for Set


      if (n === "Set") {
        return {
          specialReplacerForES6DataStructure_dataType: 'Set',
          specialReplacerForES6DataStructure_value: Array.from(v.entries())
        };
      } // TODO: enumerable objects
      //if (n === "WeakMap" || n == "WeakSet")
      // return ...
      // signature, we use the reverse of `@object`


      return 'tcejbo@' + n;
    });
  }

  function addToLogs(api, getOrSet, funcOrValue, funcArgvs, funcReturnValue) {
    konsole.log("%c".concat(api, " %c").concat(getOrSet, " %c").concat(funcArgvs, " %c").concat(funcOrValue, " = %c").concat(funcReturnValue), 'color: white', 'color: lime', 'color: yellow', 'color: aqua', 'color: orange'); // Skip first 4: they are our defined functions

    var stacks = []; // avoid network requests

    var _iterator3 = _createForOfIteratorHelper(stackTrace.StackTrace.getSync({
      offline: true
    }).slice(4)),
        _step3;

    try {
      for (_iterator3.s(); !(_step3 = _iterator3.n()).done;) {
        var stack = _step3.value;
        stacks.push({
          line: stack.lineNumber,
          column: stack.columnNumber,
          filename: stack.fileName || null,
          // JSON.stringify will skip undefined, so we use null here
          func: stack.functionName || null,
          source: stack.source || null
        });
      }
    } catch (err) {
      _iterator3.e(err);
    } finally {
      _iterator3.f();
    }

    var key = jsonDecycleAndStringify({
      api: api,
      method: getOrSet,
      type: funcOrValue,
      filename: stacks[0].filename || '',
      source: stacks[0].source || '',
      argv: argvBlacklist.has(api) ? '__omitted__' : funcArgvs,
      ret: retBlacklist.has(api) ? '__omitted__' : funcReturnValue,
      stacks: omitStacktrace ? '__omitted__' : stacks
    });
    var value = 1 + (logs.has(key) ? logs.get(key) : 0);
    logs.set(key, value);
  }

  function instrumentObject(object, objectName) {
    if (typeof object === 'undefined') throw Error(objectName + " is undefined"); // https://stackoverflow.com/a/22658584/11712282
    // for-in loop will capture inherited keys whereas getOwnPropertyNames traverses its own properties.
    // Object.keys(a) will only return all enumerable own properties.

    var properties = Object.getOwnPropertyNames(object);

    for (var i = 0; i < properties.length; i++) {
      try {
        instrumentObjectProperty(object, objectName, properties[i]);
      } catch (e) {
        konsole.error("Cannot instrment ".concat(objectName, ".").concat(properties[i], ": ").concat(e.message));
      }
    }
  }

  var constructorCache = new Map();
  function isConstructor(f) {
      if (f in constructorCache) {
          return constructorCache[f];
      }
      var result = true;
      try {
          new f();
      } catch (err) {
          // verify err is the expected error and then
          result = false;
      }
      constructorCache[f] = result;
      return result;
  }

  function definePropertyWrapper(object, objectName, propertyName, propDesc) {
    // We overwrite both data and accessor properties as an instrumented
    // accessor property
    var isDataDescriptor = ('writable' in propDesc);
    Object.defineProperty(object, propertyName, {
      configurable: true,
      get: function get() {
        var origProperty = isDataDescriptor ? propDesc.value : propDesc.get.call(this); // Workaround for localStorage and sessionStorage
        // Do not log the additional function call

        if (objectName == "localStorage" && this !== window.localStorage || objectName == "sessionStorage" && this !== window.sessionStorage) {
          if (typeof origProperty == 'function') {
            return function () {
              var arr = Array.from(arguments);
              var result = origProperty.apply(this, arguments);
              return result;
            };
          } else {
            return origProperty;
          }
        } // Log `gets` except those that have instrumented return values
        // * All returned functions are instrumented with a wrapper
        // * Returned objects may be instrumented if recursive
        //   instrumentation is enabled and this isn't at the depth limit.


        if (typeof origProperty == 'function') {
          if (isConstructor(origProperty)) {
            return function () {
              var arr = Array.from(arguments);
              var construct = origProperty.constructor.bind(this, arguments);
              var result = new construct();
              addToLogs(objectName + '.' + propertyName, 'GET', 'function', arr, result);
              return result;
            };
          } else {
            return function () {
              var arr = Array.from(arguments);
              var result = origProperty.apply(this, arguments);
              addToLogs(objectName + '.' + propertyName, 'GET', 'function', arr, result);
              return result;
            };
          }
        } else {
          addToLogs(objectName + '.' + propertyName, 'GET', 'value', undefined, origProperty);
          return origProperty;
        }
      },
      set: function set(value) {
        // The setter only make sense if
        // 1. Data descriptor: writable
        // 2. Accessor descriptor: setter is not undefined
        // Does the return value of setter really matter?
        var returnValue = value;

        if (isDataDescriptor) {
          // We modify this value only if it's writable
          if (propDesc.writable) {
            propDesc.value = value;
          }

          addToLogs(objectName + '.' + propertyName, 'SET', 'value', value, undefined);
        } else {
          // accessor descriptor
          // we invoke this setter only if it's defined
          if (propDesc.set) {
            returnValue = propDesc.set.call(this, value);
          }

          addToLogs(objectName + '.' + propertyName, 'SET', 'function', value, returnValue);
        }

        return returnValue;
      }
    });
  }

  function instrumentObjectProperty(object, objectName, propertyName) {
    if (!object) throw Error("Object does not exist: " + objectName + "." + propertyName);
    if (apiBlacklist.has(objectName + "." + propertyName)) return konsole.warn("Ignored property in blacklist: " + objectName + "." + propertyName);
    var propDesc = Object.getOwnPropertyDescriptor(object, propertyName);

    if (!propDesc) {
      if (propertyName == "Cookie") {
        Object.defineProperty(object, "Cookie", {
          configurable: true,
          get: function get() {
            return Document.cookie;
          },
          set: function set() {
            return Document.cookie;
          }
        });
        propDesc = Object.getOwnPropertyDescriptor(object, propertyName);
      } else if (objectName == 'CSSStyleDeclaration') {
        Object.defineProperty(object, propertyName, {
          value: '',
          writable: true,
          enumerable: true,
          configurable: true
        });
        propDesc = Object.getOwnPropertyDescriptor(object, propertyName);
      } else {
        throw Error("Object " + objectName + " does not have the property " + propertyName);
      }
    }

    var isDataDescriptor = ('writable' in propDesc);

    if (isDataDescriptor) {
      if (!('value' in propDesc && 'configurable' in propDesc)) throw Error('data descriptor but it has no value and conf');
    } else {
      if (!('get' in propDesc && 'set' in propDesc && 'configurable' in propDesc)) throw Error('accessor descriptor but it has no setter, getter, conf');
    }

    if (isDataDescriptor) {
      // 1. Data descriptor, e.g. Math.PI
      //   Object { value: 3.14, writable: false, enumerable: false, configurable: false }
      // The only case we can't do much is non-configurable and non-writable.
      // If it's not configurable, we cannot modify or override those attributes
      // See https://javascript.info/property-descriptors#non-configurable
      if (!propDesc.configurable && !propDesc.writable) {
        return konsole.warn('Cannot instrument non-configurable and non-writable data descriptor ' + objectName + '.' + propertyName);
      } // If a property is configurable but not writable, the property
      // could be deleted and redefined. We can just overwrite `writable`.


      if (!propDesc.writable) {
        Object.defineProperty(object, propertyName, {
          writable: true
        }); // make sure it's writable now

        if (!Object.getOwnPropertyDescriptor(object, propertyName).writable) throw Error("fail to overwrite writable");
      }
    } else {
      // 2. Accessor descriptor, e.g. navigator.doNotTrack
      //   Object { get: get(), set: undefined, enumerable: true, configurable: true}
      // If it's not configurable, we cannot modify or override those attributes
      // See https://javascript.info/property-descriptors#non-configurable
      if (!propDesc.configurable) {
        return konsole.warn('Cannot instrument non-configurable accessor descriptor ' + objectName + '.' + propertyName);
      }
    }

    definePropertyWrapper(object, objectName, propertyName, propDesc);
    konsole.debug('Instrument: ' + objectName + '.' + propertyName);
  }

  function getPrototypeOf(x) {
    if (typeof x === 'function') {
      return x.prototype;
    } else if (_typeof(x) === 'object') {
      var proto = Object.getPrototypeOf(x); // If the prototype is equal to prototype of "Object"
      // that implies the object itself is already the prototype (e.g., Math, Intl)

      if (Object.getPrototypeOf(proto) === null) return x; // instrument window prototype / document prototype not work, use the object itself instead

      if (x === window || x === document) return x;
      return Object.getPrototypeOf(x);
    } else {
      throw Error("Cannot get prototype of " + x);
    }
  }

  function instrumentApi(api) {
    // Css font fingerprinting
    // firefox: CSS2Properties
    // chrome: CSSStyleDeclaration, but the value is optionals
    // Only firefox support this attribute
    //if (api == 'CSS2Properties' && !isFirefox)
    //  return;
    try {
      if (api.includes(".")) {
        var split = api.split(".");
        var objectName = split[0];
        var propertyName = split[1];
        instrumentObjectProperty(getPrototypeOf(window[objectName]), objectName, propertyName);
      } else {
        var _objectName = api;
        instrumentObject(getPrototypeOf(window[_objectName]), _objectName);
      }
    } catch (e) {
      konsole.error("Cannot instrument API " + api + ": " + e.message);
    }
  } // By default, we won't expose global variables / log to console
  // unless this is test environment


  var logs = new Map();

  var nop = function nop() {};

  var konsole = {
    debug: nop,
    log: nop,
    info: nop,
    warn: nop,
    error: nop
  };

  if (location.hostname == "localhost" || location.hostname == "127.0.0.1") {
    window.logs = logs; // intended shallow copy

    konsole = console;
  } // must be in the format "<object>.<property>"


  var apiBlacklist = new Set([// "navigator.buildID",
  "Crypto.getRandomValues", "Math.sqrt", "Math.pow", "Math.min", "Math.max", "Math.abs", "Math.round", "Math.random", "Math.floor"]); // omit arguments in logs

  var argvBlacklist = new Set(["Performance.now", "Performance.timing", "Date.getTime", "Date.valueOf", "localStorage.getItem", "localStorage.setItem", "localStorage.removeItem", "sessionStorage.getItem", "sessionStorage.setItem", "sessionStorage.removeItem"]); // omit return values in logs

  var retBlacklist = new Set(["Performance.now", "Performance.timing", "Date.getTime", "Date.valueOf", "localStorage.getItem", "localStorage.setItem", "localStorage.removeItem", "sessionStorage.getItem", "sessionStorage.setItem", "sessionStorage.removeItem"]); // omit stacktrace or not

  var omitStacktrace = true;
  var isFirefox = navigator.userAgent.includes('Firefox/'); // Selenium + Chromedriver-specific

  if (!isFirefox && navigator.webdriver === true) {
    // https://chromium.googlesource.com/chromium/src/+/master/chrome/test/chromedriver/js/call_function.js#244
    document.$cdc_asdjflasutopfhvcZLmcfl_ = {
      cache_: Object.create(null),
      // this function will be invoked by chromedriver
      clearStale: function clearStale() {}
    }; // Note: the chrome internal code will invoke clearStale
    // https://chromium.googlesource.com/chromium/src/+/master/chrome/test/chromedriver/js/call_function.js#436
    // this will also be included in the logs

    instrumentApi("document.$cdc_asdjflasutopfhvcZLmcfl_");
  } // instrument APIs


  ["navigator", "ServiceWorkerContainer", // This seems to cause YouTube fails to load
  //"EventTarget.addEventListener",
  "screen", "Math", "MimeType", "Plugin", "RTCPeerConnection", "HTMLCanvasElement", "CanvasRenderingContext2D", "BaseAudioContext", "AudioContext", "OscillatorNode", "AnalyserNode", "GainNode", "ScriptProcessorNode", "OfflineAudioContext", "BatteryManager", "MediaDevices", "MediaDeviceInfo", "Selection", "ClipboardEvent", "Date", "Intl", "DeviceOrientationEvent", "DeviceMotionEvent", "DeviceAcceleration", "DeviceRotationRate", "DeviceLightEvent", "DeviceProximityEvent", "UserProximityEvent", "Geolocation", "GeolocationPosition", "GeolocationCoordinates", "AbsoluteOrientationSensor", "Accelerometer", "AmbientLightSensor", "Gyroscope", "LinearAccelerationSensor", "Magnetometer", "RelativeOrientationSensor", // Chrome 72.0.3626.0 + Chromedriver 2.46 on Windows API differences
  "chrome.app", "chrome.app.RunningState", "chrome.app.InstallState", "Gamepad", "GamepadButton", "GamepadEvent", "NetworkInformation", "Performance", "WebGLRenderingContext", "WebGL2RenderingContext", "WebGLActiveInfo", "WebGLBuffer", "WebGLContextEvent", "WebGLFramebuffer", "WebGLProgram", "WebGLQuery", "WebGLRenderbuffer", "WebGLSampler", "WebGLShader", "WebGLShaderPrecisionFormat", "WebGLSync", "WebGLTexture", "WebGLTransformFeedback", "WebGLUniformLocation", "WebGLVertexArrayObject", "Document.cookie", "Crypto", // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/connect-src
  "WebSocket", "XMLHttpRequest", "window.fetch", "localStorage", "sessionStorage", "indexedDB", // TODO: will this flood the conolse?
  "CSSStyleDeclaration", "HTMLElement.offsetWidth", "HTMLElement.offsetHeight", // https://webplatform.github.io/docs/css/cssom/CSSStyleDeclaration/CSSStyleDeclaration/
  // Chrome-specific, but it should be safe to instrument this on Firefox
  "CSSStyleDeclaration.fontSize", "CSSStyleDeclaration.fontFamily", // Firefox-specific
  "CSS2Properties.fontSize", "CSS2Properties.fontFamily"].forEach(instrumentApi); // This will return a promise

  function sendData(logBucket) {
    var url = location.protocol + '//' + location.host + '/non-exist-api';
    var option = {
      method: 'POST',
      credentials: 'include',
      body: jsonDecycleAndStringify(logBucket)
    };
    return new Promise(function (resolve, reject) {
      fetch(url, option).then(function (res) {
        return res.text();
      }).then(function (text) {
        if (text !== "ACK") reject("Fail to send log to ".concat(url, " due to ACK not received, but receive ").concat(text));
        resolve();
      }).catch(function (e) {
        return reject("Fail to send log to ".concat(url, " due to ").concat(e.message));
      });
    });
  } // https://stackoverflow.com/a/326076


  function isIframe() {
    try {
      return window.self !== window.top;
    } catch (e) {
      return true;
    }
  }

  window.scollBehavior = "undefined";

  function determineScrollType() {
    var yPos1 = window.scrollY;
    var scrollHeight1 = document.body.scrollHeight;
    window.scrollTo({
      top: scrollHeight1,
      left: 0,
      behavior: 'smooth'
    });
    window.setTimeout(function () {
      if (scrollHeight1 <= screen.height || window.scrollY && window.scrollY == yPos1) {
        // Scrolling is not necessary or position did not change
        window.scrollBehavior = "no-scroll";

        if (window.getComputedStyle(document.body).overflow == "hidden") {
          // Part of the page is hidden
          window.scrollBehavior = "blocked-scroll";
        } else {
          var passInputs = document.querySelectorAll('input[type=password]');

          if (passInputs.length > 0) {
            window.scrollBehavior = "login-page";
          }
        }
      } else {
        if (document.body.scrollHeight > scrollHeight1) {
          // Document has expanded
          window.scrollBehavior = "dynamic-scroll";
        } else {
          window.scrollBehavior = "static-scroll";

          if ((window.scrollY - yPos1) / screen.height < 1.5) {
            // Scroll delta is small, maybe login page?
            var passInputs = document.querySelectorAll('input[type=password]');

            if (passInputs.length > 0) {
              window.scrollBehavior = "login-page";
            }
          }
        }
      }
    }, 2000);
  }

  function collectData() {
    var logBucket = Object.create(null); // 1. js instrumentation log
    // we stringify the log here because accesing cookies/storage later will add unwanted logs

    logBucket['js_logs'] = [];

    var _iterator4 = _createForOfIteratorHelper(logs.entries()),
        _step4;

    try {
      for (_iterator4.s(); !(_step4 = _iterator4.n()).done;) {
        var _step4$value = _slicedToArray(_step4.value, 2),
            _key2 = _step4$value[0],
            count = _step4$value[1];

        var key_obj = JSON.parse(_key2);
        key_obj.count = count;
        logBucket['js_logs'].push(key_obj);
      }
    } catch (err) {
      _iterator4.e(err);
    } finally {
      _iterator4.f();
    }

    ; // 2. Non-http-only cookies

    logBucket['document_cookies'] = document.cookie; // 3. localStorage and sessionStorage

    logBucket['local_storage'] = Object.create(null);

    for (var i = 0; i < localStorage.length; i++) {
      var key = localStorage.key(i);
      logBucket['local_storage'][key] = localStorage.getItem(key);
    }

    logBucket['session_storage'] = Object.create(null);

    for (var _i2 = 0; _i2 < sessionStorage.length; _i2++) {
      var _key = sessionStorage.key(_i2);

      logBucket['session_storage'][_key] = sessionStorage.getItem(_key);
    } // 4. indexed_db
    // TODO: enumerating all index databases is a browser-specific api
    // https://github.com/w3c/IndexedDB/issues/31
    // 5. iframe or not


    logBucket['is_iframe'] = isIframe(); // 6. Its referrer (available only when this is in iframe)

    logBucket['referrer'] = isIframe() ? document.referrer : ""; // 7. All iframes in this frame

    var iframe_objects = [];

    var _iterator5 = _createForOfIteratorHelper(document.querySelectorAll('iframe')),
        _step5;

    try {
      for (_iterator5.s(); !(_step5 = _iterator5.n()).done;) {
        var iframe = _step5.value;
        var iframe_object = {}; // so it can be JSON stringified

        var _iterator6 = _createForOfIteratorHelper(iframe.attributes),
            _step6;

        try {
          for (_iterator6.s(); !(_step6 = _iterator6.n()).done;) {
            var attr = _step6.value;
            iframe_object[attr.name] = attr.value;
          }
        } catch (err) {
          _iterator6.e(err);
        } finally {
          _iterator6.f();
        }

        iframe_objects.push(iframe_object);
      }
    } catch (err) {
      _iterator5.e(err);
    } finally {
      _iterator5.f();
    }

    logBucket['iframes'] = iframe_objects; // 8. current url (window.location)

    logBucket['url'] = location.toString(); // 9. Scrolling behavior of the page

    logBucket['scroll_behavior'] = window.scrollBehavior;
    return logBucket;
  } // this variable will be dynamically replaced


  var time = __ENDING_TIMESTAMP_MSEC >= new Date().getTime() ? __ENDING_TIMESTAMP_MSEC - new Date().getTime() : 0; // Scroll at half-time

  if (__SHOULD_SCROLL) {
    setTimeout(function () {
      determineScrollType();
    }, time / 2);
  }

	if (__OWPM86) {
		// Override maxTouchPoints
		Object.defineProperty(navigator, 'maxTouchPoints', {get: function() {return 5;}});
		// Override AudioContext.sampleRate
		var __AudioContext = window.AudioContext;
		window.AudioContext = function() { 
			var ac = new __AudioContext(); 
			Object.defineProperty(ac, 'sampleRate', {value: 48000}); 
				return ac; 
		}
		// Override navigator.mediaDevices
		window.navigator.mediaDevices.__enumerateDevices = window.navigator.mediaDevices.enumerateDevices;
    window.navigator.mediaDevices.enumerateDevices = function() {
      	return new Promise(function(resolve, reject) {
        	window.navigator.mediaDevices.__enumerateDevices().then(function(devices) {
          	var newDevices = [];
            var audioCtr = 0;
            devices.forEach(function(device) {
            	if (device.kind == 'audioinput') {
              	if (audioCtr < 1) {
                	newDevices.push(device);
                  audioCtr += 1;
                }
             	} else {
              	newDevices.push(device);
              }
           	});
            resolve(newDevices);
          }).catch(function(err) {
            reject(err);
          });
        });
     }
	}

  setTimeout(function () {
    var logBucket = collectData();
    sendData(logBucket).catch(function (msg) {
      throw msg;
    }).then(function () {
      if (!isIframe()) {
        // NOTE: the main frame will await for 5 more seconds
        // to ensure iframes finish first. (most iframes will finish in just 2 seconds)
        setTimeout(function () {
          return location = 'http://240.240.240.240/stop';
        }, 5000);
      }
    });
  }, time);
}();
