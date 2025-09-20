### Executive summary

Build a modular ML pipeline that: (1) acquires and subsets NASA Earth observation time series for a user-selected point or polygon and day-of-year window via OPeNDAP/GES DISC services and Giovanni time-series APIs, (2) computes climatologies and probabilities for “very hot,” “very cold,” “very windy,” “very wet,” and “very uncomfortable” based on percentile thresholds and extreme-value fits, and (3) returns compact JSON/CSV with metadata, units, and provenance for direct UI rendering and download, consistent with challenge guidance and resources.[^1][^2][^3][^4][^5][^6]

***

### Scope and assumptions

- Only ML/data pipeline; backend and frontend are complete and will call these modules via internal interfaces.[^1]
- Historical-probability product, not a short-term forecast; use long-term NASA time series with day-of-year filtering and trend signals to show changing odds.[^2][^7][^1]
- Core variables: temperature (e.g., 2m air temperature), precipitation rate/accumulation, wind speed at 10m, humidity or heat index proxy, optional cloud cover; prioritize widely-available datasets in GES DISC.[^3][^4][^6][^1]
- Data services: GES DISC OPeNDAP/Hyrax for subsetting and programmatic access, Giovanni programmatic time-series where convenient; adhere to Earthdata usage patterns.[^4][^6][^3]

***

### Team structure

- ML Engineer A — Data Acquisition \& Subsetting: dataset selection, credentialed OPeNDAP/HTTPS access, spatial-temporal subsetting, unit harmonization, metadata capture, caching.[^6][^3][^4]
- ML Engineer B — Climatology \& Probability Modeling: thresholds, percentiles, extreme-value modeling (GEV) for tails, trend estimation, uncertainty, QA checks.[^8][^7]
- ML Engineer C — Serving Layer \& Contracts: inference assembly, response schemas, performance optimization, reproducibility bundles, CSV/JSON export with metadata and source links.[^3][^4][^1]

***

### Interfaces with backend/frontend

- Input contract (from backend): location geometry (point lat,lon; or polygon GeoJSON), target day-of-year (single day), optional time window (±N days around DOY), variable set requested, year range.[^1]
- Output contract (to backend/UI): probabilities for each condition, summary stats (mean, median, percentiles), trend info, uncertainty bounds, sample sizes, provenance metadata (dataset ids, services, query ranges), downloadable CSV/JSON links.[^1]

***

### Datasets and access

Preferred sources (choose one per variable based on coverage and latency; prioritize consistent temporal coverage):

- Precipitation: GPM IMERG Final Run (daily/monthly) with liquid/phase information and long multi-year history; strong for “very wet” probabilities.[^5]
- Temperature and wind: MERRA‑2 reanalysis (hourly/daily radiation, 2m temperature, 10m wind) via GES DISC with robust subsetting services.[^9][^6]
- Humidity/heat discomfort: MERRA‑2 specific humidity/relative humidity; derive heat index and “very uncomfortable.”[^9][^6]
- Access patterns: OPeNDAP URLs with server-side slicing; Giovanni Time Series API where supported for point extracts.[^4][^6][^3]

Engineer A will finalize dataset-product IDs and variable names with concrete OPeNDAP URLs and example slices.[^6][^3][^4]

***

### Definitions of conditions

- Very hot: probability that daily max $T$ exceeds threshold (default 90°F/32.2°C, user-configurable) on target DOY window and location.[^2][^1]
- Very cold: probability daily min $T$ below threshold (default 32°F/0°C).[^2][^1]
- Very windy: probability daily mean or max wind speed above threshold (default 10 m/s; user-configurable).[^2][^1]
- Very wet: probability daily precipitation above threshold (e.g., >10 mm/day), plus optional IMERG phase hints.[^5][^1][^2]
- Very uncomfortable: composite probability where any of heat index exceeding unsafe threshold, high humidity with heat, or wind-chill cold crosses limits; provide explanation fields.[^1][^2]

Engineer B implements defaults and allows threshold overrides; tie to UI controls already available.[^1]

***

### End-to-end flow

1) Resolve geometry and DOY window; 2) Query time series across selected years; 3) Aggregate days within DOY window per year; 4) Compute probabilities from empirical distribution and optionally fit GEV for extremes; 5) Estimate trend over years; 6) Return JSON/CSV with stats, plots-ready series, and provenance.[^7][^8][^3][^4][^6][^1]

***

### ML Engineer A — Data Acquisition \& Subsetting

Objectives

- Provide robust, authenticated, rate-limited data loaders for selected datasets through OPeNDAP/HTTPS with spatial/temporal slicing, returning harmonized xarray DataArrays with units and CF metadata.[^3][^6]
- Support point and polygon: nearest-neighbor for points; area-weighted mean for polygons.[^6][^3]
- Expose a simple Python API to higher layers.

Core tasks

- Pick datasets and variables with IDs and units, e.g., IMERG precipitation (mm/day), MERRA‑2 2m temperature (K; convert to °C/°F), 10m wind (m/s), humidity (%). Provide a table of product IDs and variable names.[^5][^9][^6]
- Implement OPeNDAP client using pydap/xarray open_dataset with query slicing for time, lat, lon; cache URL patterns; retry logic with backoff. Include examples for point and bbox slicing.[^10][^3][^6]
- Implement DOY-window extractor: for a target DOY and ±W days, select all dates each year in [year_min, year_max] and emit masked arrays.[^1]
- Unit normalization: Kelvin→°C/°F, precipitation to mm/day, wind m/s; store units in attrs.[^3]
- Metadata and provenance: attach dataset citation string, OPeNDAP URL templates, time coverage, version; output alongside data.[^6][^3]
- Performance: lazy loads, chunking, minimal variable requests, restrict dims server-side; configurable cache directory and eviction.[^3][^6]
- Optional Giovanni time-series: implement adapter to fetch point series where supported for quick prototyping.[^4]

Deliverables

- Module: data_access/loader.py with functions:
    - get_timeseries(dataset_id, var, geom, t0, t1, agg="daily|monthly") -> xarray.DataArray. [^3][^6]
    - doy_window(data, doy, window_days, years) -> list[DataArray by year].[^3]
    - area_reduce(data, polygon, method="mean") -> DataArray.[^3]
- Config: data_access/datasets.yaml with product IDs, variables, units, example OPeNDAP endpoints.[^9][^5][^6]
- Notebook: notebooks/data_access_smoke.ipynb demonstrating slices and metadata capture.[^4][^6][^3]

Quality gates

- Verify units and sample statistics versus Giovanni plots/time-series for a test point.[^4]
- Confirm server-side slicing by inspecting request URLs and response shapes.[^6][^3]

***

### ML Engineer B — Climatology \& Probability Modeling

Objectives

- Convert historical DOY-window slices into probabilities for each condition with uncertainty; fit GEV for extremes; compute linear trends and visualize-ready summaries.[^8][^7]

Core tasks

- Empirical probabilities: for each year’s DOY-window, compute indicator of condition met; aggregate across years to get $p$ and Wilson interval; return distribution percentiles (p10, p50, p90).[^1]
- Thresholds: implement defaults and allow variable-specific user thresholds; expose helper to translate common units.[^1]
- Extremes: fit GEV to annual maxima for heat or daily precip extremes; use scipy.stats.genextreme or L‑moments to estimate tail probabilities for exceedances; include goodness-of-fit checks (KS/AD).[^8]
- Trend: regress annual condition indicator or annual maxima against year; report slope, p-value; optionally Theil–Sen robust slope.[^7]
- Composite “very uncomfortable”: define as OR rule across heat index > threshold, RH high with heat, or wind chill extreme; compute probability and provide which component triggered most years.[^1]
- Seasonality: ensure per-DOY window computation, not entire-year averages; allow window widening to stabilize estimates in sparse regions.[^1]
- Uncertainty: report sample size Nyears, binomial CI, and for GEV tail, parametric CI by delta method or bootstrap.[^7][^8]

Deliverables

- Module: modeling/probability.py:
    - compute_empirical_prob(series, comparator, threshold) -> p, ci_low, ci_high.[^1]
    - fit_gev_annual_max(series) -> params, gof, tail_p(threshold).[^8]
    - trend_linear(x=year, y=indicator/maxima) -> slope, p, r2.[^7]
    - compute_condition_probs(data_bundle, config) -> dict of results per condition.[^1]
- Config: modeling/thresholds.yaml with defaults for each variable and “very uncomfortable” composite.[^1]
- Notebook: notebooks/modeling_validation.ipynb with sanity checks and example fits.[^8][^7]

Quality gates

- Validate GEV fit on known station-like series; report KS/AD stats and visually check QQ; fall back to empirical only if fit fails.[^8]
- Demonstrate monotonic trend detection on synthetic warming series per methodology discussed in literature.[^7]

***

### ML Engineer C — Serving Layer \& Contracts

Objectives

- Provide the thin inference layer that orchestrates A and B, enforces I/O contracts, outputs compact JSON and downloadable CSV with metadata, and meets latency targets via caching/pruning.[^1]

Core tasks

- Define request schema: geometry (Point or Polygon GeoJSON), day_of_year, window_days, variables requested, thresholds override, year range, aggregation method.[^1]
- Orchestration: call A to fetch normalized DOY-window slices; pass to B for probabilities/GEV/trends; assemble single response with versions and provenance.[^7][^4][^6][^8][^3]
- Response schema:
    - conditions: for each label, fields {probability, ci, threshold, method, Nyears}.[^1]
    - stats: variable summaries (mean, median, p10/p90) over DOY-window.[^1]
    - extremes: tail probability estimates with model params and GOF if available.[^8]
    - trend: slope per decade and p-value; brief natural-language explanation.[^7]
    - metadata: dataset ids, units, service URLs used, query ranges, processing version.[^4][^6][^3]
    - downloads: presigned paths to CSV and JSON with columns [date, var, value, flags].[^1]
- Serialization: ensure floats <3 decimals in API layer; include units; ensure fields map to UI components like bell curves or time series.[^1]
- Performance: cache by (geom hash, DOY, window, years, dataset version, thresholds); parallelize per-variable fetch; short-circuit for repeated calls.[^3]
- Error handling: partial results when one dataset fails; embed warnings; return HTTP 200 with per-variable status since backend is done.[^1]
- Reproducibility bundle: embed commit hashes of data_access/modeling, datasets.yaml and thresholds.yaml versions.[^1]

Deliverables

- Module: service/inference.py:
    - run_inference(request: dict) -> response: dict per schema.[^1]
    - export_csv(data_bundle) -> path; export_json(response) -> path.[^1]
- JSON Schemas: contracts/request.schema.json and contracts/response.schema.json.[^1]
- Notebook: notebooks/e2e_demo.ipynb showing end-to-end request→response with example.[^1]

Quality gates

- Load-test synthetic 10 requests with diverse geoms; ensure 95th percentile latency under agreed budget with caching.[^1]
- Contract tests: strict validation with example payloads covering thresholds overrides and polygon inputs.[^1]

***

### Data choices and variable mapping

Recommended initial mappings for broad coverage and speed to value:

- Precipitation: GPM IMERG Final Run V07 Daily (mm/day), variable e.g., precipitationCal; use daily accumulation; IMERG includes phase probability helpful for context.[^5]
- Temperature: MERRA‑2 2m temperature (convert K→°C/°F) for daily max/min via hourly aggregation if needed.[^9][^6]
- Wind: MERRA‑2 10m wind speed (m/s); compute daily max or mean; choose consistent definition.[^9][^6]
- Humidity/comfort: MERRA‑2 relative humidity or specific humidity to compute heat index; optionally combine with temperature.[^9][^6]

Engineer A documents exact product short names and dimension orders in datasets.yaml for reproducibility.[^6][^9]

***

### Probability and extremes methodology

- Empirical probability: for each year, flag if any DOY-window day meets condition; probability = sum(flags)/Nyears; CI via Wilson score interval to avoid 0 or 1 degeneracy.[^1]
- GEV extremes: derive annual maxima series for variable (e.g., daily max temperature, daily precip) and fit GEV; compute exceedance probabilities for user thresholds; backstop to empirical if GOF fails.[^8]
- Trend: linear regression of annual indicators or maxima vs year; report slope per decade, p-value; mention that warming accelerates record-shattering probabilities, aligning with literature.[^7]

These choices align with challenge expectations for probabilities and extreme likelihoods from historical data rather than dynamic forecasts.[^2][^1]

***

### File formats and metadata

- JSON response for UI: concise fields with units and methods strings; friendly “explanation” per condition.[^1]
- CSV download: columns [date, variable, value, unit, exceeded_flag, window_id]; include header comments with dataset version, OPeNDAP URLs, and time coverage.[^6][^3][^1]
- Provenance: include list of dataset landing IDs, request AOI, DOY window, years used, and any missing dates.[^6][^3]

***

### Non-functional requirements

- Reproducibility: pin product versions; version config files; hash geometry and parameters into cache keys.[^1]
- Latency: target under 6–8 seconds for cache warm calls for one point and three variables over 25–30 years; parallelize variable fetch and precompute DOY indices.[^3]
- Robustness: graceful degradation if one dataset is unavailable; return partial results with warnings and provenance intact.[^1]

***

### Milestones and integration plan

Week 1

- A: finalize datasets.yaml with tested OPeNDAP slices and units; implement point/polygon extraction and DOY windowing.[^6][^3]
- B: implement empirical probabilities and thresholds with tests; draft GEV fit function and GOF checks.[^8]
- C: define request/response schemas; stub inference orchestrator and JSON serializer; integrate A’s API.[^1]

Week 2

- A: caching, retries, and Giovanni adapter for point checks; validation notebook.[^4][^3]
- B: finalize GEV workflow, uncertainty, and trend; composite uncomfortable; validation notebook.[^7][^8]
- C: CSV export, provenance, caching keys, error mapping; end-to-end notebook; load tests.[^1]

***

### Test scenarios

- Urban park in June (hot/humid) vs mountain trail in October (cold/windy), same DOY window, compare probabilities and trends; verify UI-friendly summaries.[^2][^1]
- Threshold overrides: e.g., hot at 35°C vs 32°C, see sensitivity; ensure response carries threshold used.[^1]
- Polygon vs point: average over a lake polygon vs shoreline point; confirm area-reduction works.[^3]
- Data outage drill: simulate IMERG unavailable and validate partial results flow.[^5]

***

### Security and access

- Use Earthdata authentication as required for certain datasets; store tokens securely in environment variables consumed only by data_access.[^6]
- Respect usage limits; add backoff and minimal variable requests per call.[^3][^6]

***

### Handover checklists

Engineer A

- datasets.yaml with product short names, variables, units, dimension order, sample OPeNDAP URLs.[^9][^6]
- loader.py with docstrings and examples; caching policy documented.[^3]

Engineer B

- probability.py with unit-tested functions; thresholds.yaml with defaults and notes.[^1]
- validation notebook showing empirical vs GEV consistency and trend demo.[^8][^7]

Engineer C

- request/response JSON schemas and inference.py orchestrator; CSV/JSON exporters; provenance fields documented for UI mapping.[^6][^3][^1]

***

### References and resources

- Challenge brief and UX expectations for probabilities and dashboards.[^11][^2][^1]
- GES DISC OPeNDAP subsetting tutorials and user guide for programmatic access.[^6][^3]
- Giovanni time-series Python examples for quick validation and comparisons.[^4]
- IMERG precipitation product details including phase probability field.[^5]
- Extreme value modeling references and practice for weather extremes.[^7][^8]


[^1]: https://www.spaceappschallenge.org/2025/challenges/will-it-rain-on-my-parade/

[^2]: https://www.youtube.com/watch?v=FMUuEAjQ9Lo

[^3]: https://www.earthdata.nasa.gov/learn/tutorials/get-started-opendap-subset-download-nasa-earth-data

[^4]: https://github.com/nasa/gesdisc-tutorials

[^5]: https://gpm.nasa.gov/data/imerg

[^6]: https://www.earthdata.nasa.gov/engage/open-data-services-software/earthdata-developer-portal/opendap/user-guide

[^7]: https://pmc.ncbi.nlm.nih.gov/articles/PMC7617090/

[^8]: https://www.scielo.br/j/brag/a/RgLR5hqHfG3jFD4g9R7nbGr/?lang=en

[^9]: https://forum.earthdata.nasa.gov/viewtopic.php?t=5678

[^10]: https://sesync-ci.github.io/fldas-pipeline-lesson/index.html

[^11]: https://www.spaceappschallenge.org/2025/challenges/

[^12]: https://www.spaceappschallenge.org/2025/challenges/will-it-rain-on-my-parade/?tab=teams

[^13]: https://www.spaceappschallenge.org/2025/find-a-team/rain-guardians/

[^14]: https://www.nasa.gov/nasa-space-apps-challenge-2025/

[^15]: https://www.spaceappschallenge.org/2025/find-a-team/forecast-fusion/

[^16]: https://www.instagram.com/p/DODhHHoiIJr/-𝙲𝚑𝚊𝚕𝚕𝚎𝚗𝚐𝚎-18𝐖𝐢𝐥𝐥-𝐈𝐭-𝐑𝐚𝐢𝐧-𝐎𝐧-𝐌𝐲-𝐏𝐚𝐫𝐚𝐝𝐞-planning-an-outdoor-adventure-like-a-hike/

[^17]: https://reports.weforum.org/docs/WEF_Climate_Adaptation_Unlocking_Value_Chains_with_the_Power_of_Technology_2025.pdf

[^18]: https://www.spaceappschallenge.org/2025/find-a-team/bluesky-minds/

[^19]: https://aclanthology.org/anthology-files/pdf/climatenlp/2025.climatenlp-1.pdf

[^20]: https://stackoverflow.com/questions/40088745/how-to-download-nasa-satellite-opendap-data-using-python

