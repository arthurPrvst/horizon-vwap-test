# VWAP test

Has been run with JDK 11

## Set up 

`./gradlew install` 

`./gradlew build` 

`./gradlew test` or `./gradlew --rerun-tasks` to force re-run of up-to-date tests

## Logging

All logs levels are displayed in the console. 

DEBUG logs (for vwap > fairValue on the TEST_PRODUCT) are outputed in a rolled file in the `./target/log4j/rollout/` directory.

Result in the file:
`tail -50f ./target/log4j/rollout/test-product.log`
<pre>
pc29:horizon-vwap-test arthurprovost$ tail -50f ./target/log4j/rollout/test-product.log
16-09-2021 12:37:56,203 [DEBUG] [pool-1-thread-9] hsoft.services.DataListenerService: VWAP (100.047619047619) > FairValue (100.0)
16-09-2021 12:37:56,257 [DEBUG] [pool-1-thread-11] hsoft.services.DataListenerService: VWAP (101.094339622642) > FairValue (101.0)
16-09-2021 12:37:56,310 [DEBUG] [pool-1-thread-11] hsoft.services.DataListenerService: VWAP (101.590909090909) > FairValue (101.5)
16-09-2021 12:37:56,315 [DEBUG] [pool-1-thread-7] hsoft.services.DataListenerService: VWAP (101.590909090909) > FairValue (101.0)
</pre>

## Test

`FairValueTest`, `VwapTest`, `ExecutionsTest`, `WrongDataTest`  
