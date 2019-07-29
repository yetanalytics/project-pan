.PHONY: clean test

clean:
	rm -rf target

test: clean
	clojure -A:test:runner

