.PHONY: clean test

clean:
	rm -rf target
	rm -rf cljs-test-runner-out

test-clj:
	clojure -M:test:runner-clj

test-cljs:
	clojure -M:test:runner-cljs

ci: clean test-clj test-cljs	
