# NoiseModelling 5.0.1 — IndexOutOfBoundsException in PathFinder.computeReflexion: reproduction bundle

Stock-only inputs and script for the upstream issue. See the issue text for the exact steps.

## Summary

The unmodified NoiseModelling 5.0.1 no-GUI distribution exits with code 1 while running `Noise_level_from_traffic.groovy` on a synthetic road/receiver/building scene. The exception is `IndexOutOfBoundsException: Index 0 out of bounds for length 0` at `PathFinder.computeReflexion(PathFinder.java:761)`. No complete `RECEIVERS_LEVEL.csv` is produced.

We retained three failing inputs with barrier lengths **50, 250 and 350 metres**. These are observations for the exact scene geometries below, rather than a claim that those lengths always fail. In the same scene, the separately prepared 249 m and 349 m cases completed successfully. The fixture has not yet been reduced to a minimal scene, and newer releases have not been tested for this report.

## Version and runtime

- Release: [NoiseModelling v5.0.1](https://github.com/Universite-Gustave-Eiffel/NoiseModelling/releases/tag/v5.0.1), asset `NoiseModelling_without_gui-5.0.1.zip`.
- Release ZIP SHA-256: `876ed1c69525ffa999779a0c4d3d00898d693f6e058fc951d11b4068eddb9f86`.
- Linux x86-64, Java 17. The recorded JVM version output was:

```text
openjdk version "17.0.18-internal" 2026-01-20
OpenJDK Runtime Environment (build 17.0.18-internal+0-adhoc.rattler.src)
OpenJDK 64-Bit Server VM (build 17.0.18-internal+0-adhoc.rattler.src, mixed mode, sharing)
```

- JVM options: `-Xms128m -Xmx4g -XX:ActiveProcessorCount=1`.
- `confThreadNumber=1`; no Java agent or engine patch was loaded.
- Both diffraction flags enabled, reflection order 1, maximum source distance 150 m, maximum reflection distance 50 m, `confMaxError=0`.
- No explicit DEM or ground-absorption table is passed; other options retain the release script's defaults.

## Inputs

The H2 databases contain `ROADS` (4,096 synthetic segments), `RECEIVERS` (4,096 points), and `BUILDINGS` (1,024 synthetic buildings plus one barrier). Geometry SRID is 2154. The barrier is represented as an ordinary rectangular building polygon with `PK=1000001`, height 4 m and width 0.2 m. Its centre is `(500, 300)`; the other scene objects are unchanged.

For length L, its XY ring is:

```text
(500-L/2, 299.9), (500+L/2, 299.9),
(500+L/2, 300.1), (500-L/2, 300.1), (500-L/2, 299.9)
```

The three input identifiers are:

| Pristine input filename | Length | SHA-256 before opening the H2 database |
|---|---:|---|
| `inputs/L050/h2gisdb.mv.db` | 50 m | `5a0c8d3ab3567173eafe0cf2f4118a8b85ced13be14157b858ed426c225742da` |
| `inputs/L250/h2gisdb.mv.db` | 250 m | `99d6a486c241744f31a2f1a4062017e194c8b1977030cf2df59817c757c8f711` |
| `inputs/L350/h2gisdb.mv.db` | 350 m | `c79a38072af74b55213350eed56a23b2b8fe32f97a505a4542f4bdb57ded0647` |

Each pristine file is 2,314,240 bytes. H2 modifies the working database during a run, including failed runs, so these hashes identify the **unopened input copies**, not the databases left in a failed run directory. Keep the inputs untouched and use a new working copy for each attempt.

## Stock-only reproduction

1. Extract the release asset above and use the recorded Java runtime, or record the exact runtime used for a reproduction.
2. Place the three pristine databases at the paths in the table relative to the reproduction directory. Verify their SHA-256 values before opening them.
3. Save the following file as `repro_full_map.groovy`. It is the orchestration script used in the retained stock runs, with only its filename changed; its SHA-256 is `5601c4a7a96c089236740d9cb46b03f52b0b1f09d5cbb602acbd1af43e7c78a4`. It calls the distribution's existing calculation and export scripts.

```groovy
import java.sql.Connection

title='Stock full map and complete stock CSV export'
description='Public no-GUI orchestration matching the official Groovy tutorial'
inputs=[:]
outputs=[result:[name:'result',title:'result',type:String.class]]

def exec(Connection connection, Map input) {
    String stock=System.getenv('N5_STOCK')
    GroovyShell shell=new GroovyShell()
    Script noise=shell.parse(new File(stock+'/noisemodelling/wps/NoiseModelling/Noise_level_from_traffic.groovy'))
    noise.invokeMethod('exec',[connection,[tableBuilding:'BUILDINGS',tableRoads:'ROADS',tableReceivers:'RECEIVERS',confReflOrder:'1',confMaxSrcDist:'150',confMaxReflDist:'50',confThreadNumber:'1',confDiffVertical:true,confDiffHorizontal:true,confMaxError:'0',confExportSourceId:false]])
    Script exporter=shell.parse(new File(stock+'/noisemodelling/wps/Import_and_Export/Export_Table.groovy'))
    exporter.invokeMethod('exec',[connection,[tableToExport:'RECEIVERS_LEVEL',exportPath:new File('RECEIVERS_LEVEL.csv').absolutePath]])
    return 'Complete stock CSV exported'
}
```

4. From the reproduction directory, run one case in a fresh directory. Substitute the two absolute installation paths below; `N5_STOCK` is simply the script's stock-distribution path variable.

```bash
export JAVA_HOME=/absolute/path/to/the/recorded/jdk
export PATH="$JAVA_HOME/bin:$PATH"
export N5_STOCK=/absolute/path/to/NoiseModelling_without_gui-5.0.1
export NM_REPRO_ROOT="$PWD"
unset JAVA_TOOL_OPTIONS JDK_JAVA_OPTIONS _JAVA_OPTIONS CLASSPATH LD_PRELOAD LD_AUDIT LD_LIBRARY_PATH
export JAVA_OPTS='-Xms128m -Xmx4g -XX:ActiveProcessorCount=1'

nm_case="$NM_REPRO_ROOT/run_L250"
mkdir "$nm_case"
cp "$NM_REPRO_ROOT/inputs/L250/h2gisdb.mv.db" "$nm_case/h2gisdb.mv.db"
cd "$nm_case"
"$N5_STOCK/bin/wps_scripts" -w "$nm_case" -s "$NM_REPRO_ROOT/repro_full_map.groovy"
```

Repeat from the reproduction directory using fresh `run_L050`/`run_L350` directories and their corresponding pristine inputs. Do not reuse a database from an earlier attempt.

## Actual result

All three retained stock attempts returned code 1 without a complete CSV. The two 250/350 m attempts were not timeouts; the independent fresh 50 m diagnostic also ended by exception, before its time limit.

The common exception excerpt is:

```text
java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
    at jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
    at jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
    at jdk.internal.util.Preconditions.checkIndex(Preconditions.java:266)
    at java.util.Objects.checkIndex(Objects.java:361)
    at java.util.ArrayList.get(ArrayList.java:427)
    at org.noise_planet.noisemodelling.pathfinder.PathFinder.computeReflexion(PathFinder.java:761)
    at org.noise_planet.noisemodelling.pathfinder.PathFinder.rcvSrcPropagation(PathFinder.java:312)
    at org.noise_planet.noisemodelling.pathfinder.PathFinder.computeRaysAtPosition(PathFinder.java:263)
    at org.noise_planet.noisemodelling.pathfinder.ThreadPathFinder.call(ThreadPathFinder.java:71)
    at org.noise_planet.noisemodelling.pathfinder.PathFinder.run(PathFinder.java:142)
```

The [tagged 5.0.1 source at line 761](https://github.com/Universite-Gustave-Eiffel/NoiseModelling/blob/v5.0.1/noisemodelling-pathfinder/src/main/java/org/noise_planet/noisemodelling/pathfinder/PathFinder.java#L761) accesses `rayPath.get(0)` while obtaining the profile from the source to the first reflection point. This identifies the failing access; it is not yet a confirmed explanation of why this scene reaches it with an empty list.

## Expected result

A complete calculation/export for a supported scene, or a clear input-validation diagnostic if one of these geometries violates a required condition. Is there a documented restriction relevant to these rectangles or a known fix after 5.0.1? The preserved inputs and stock traces can be used to distinguish a geometry-validation issue from a reflection-path edge case.
