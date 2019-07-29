.PHONY: clean test

clean:
	rm -rf target

ci: clean
	clojure -A:test:runner

