# Stock-only witness for issue #1144 (NoiseModelling 5.0.1)

- `1144-empty-reflection-path-witness.patch`: test-only patch (new files under `src/test` only): a standalone Java witness with one building, one point source exactly on its wall and one receiver; a proposed JUnit regression whose assertion is provisional pending the agreed handling of this boundary; an optional manual runner for the three pristine inputs of this bundle. No production code, no database inputs.
- `minimal_repro_stock_5.0.1.log`: the witness run against the unmodified 5.0.1 no-GUI libraries (Java 17, one calculation thread): `IndexOutOfBoundsException` at `PathFinder.java:761`.

Licence of the patch: GPL-3.0, as upstream. Prepared as part of TAKT's upstream testing; no TAKT component is required.
