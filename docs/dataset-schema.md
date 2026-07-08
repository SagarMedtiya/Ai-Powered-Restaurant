# Zomato Dataset Schema & Calibration

This document defines the schema mapping, column usage, normalization strategy, and budget band thresholds for the Zomato restaurant dataset used in the recommendation engine.

---

## 1. CSV Schema Mapping

The raw Hugging Face CSV (`zomato.csv`) contains 17 columns. Below is the mapping from the source CSV headers to the [Restaurant](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/architecture.md#92-domain-model-java-records) Java domain record:

| Source CSV Header | Data Type | Mapped Field | Target Field Type | Normalization & Mapping Rules |
| :--- | :--- | :--- | :--- | :--- |
| **name** | String | `name` | `String` | Raw string (trimmed). |
| **listed_in(city)** | String | `city` | `String` | Represents the macro-city area (e.g., `"Banashankari"`, `"Basavanagudi"`). Normalised to lowercase for comparisons. |
| **location** | String | `location` | `String` | Represents the micro-locality neighborhood (e.g. `"2nd Stage, Banashankari"`). Normalised to lowercase for comparisons. |
| **cuisines** | String | `cuisines` | `List<String>` | Comma-separated string. Normalised by splitting by `,`, trimming whitespace, and storing as a list. Defaults to `["Other"]` if blank. |
| **rate** | String | `rating` | `double` | Formatted as `"4.1/5"`, `"NEW"`, `"-"`, or blank. Strips out `"/5"` and parses double value. Maps `"NEW"`, `"-"`, or blanks to `0.0`. |
| **approx_cost(for two people)** | String | `costForTwo` | `Integer` | Represents the estimated cost for two. Cleans commas (e.g., `"1,200"` -> `1200`), non-digits, and parses to `Integer`. Mapped to `null` if missing. |
| *url* | String | *Unmapped* | - | Ignored. Zomato website URL. |
| *address* | String | *Unmapped* | - | Ignored. Detailed physical address. |
| *online_order* | String | *Unmapped* | - | Ignored. Flags whether online ordering is active (`"Yes"`/`"No"`). |
| *book_table* | String | *Unmapped* | - | Ignored. Flags whether table booking is available (`"Yes"`/`"No"`). |
| *votes* | Integer | *Unmapped* | - | Ignored. Number of ratings count. |
| *phone* | String | *Unmapped* | - | Ignored. Contact phone numbers. |
| *rest_type* | String | *Unmapped* | - | Ignored. Establishment type (e.g., `"Casual Dining"`, `"Quick Bites"`). |
| *dish_liked* | String | *Unmapped* | - | Ignored. Popular items liked by visitors. |
| *reviews_list* | String/JSON | *Unmapped* | - | Ignored. Contains long review lists and rating metrics. Ignored to optimize memory usage (ADR-003). |
| *menu_item* | String/JSON | *Unmapped* | - | Ignored. Dishes available on the menu. |
| *listed_in(type)* | String | *Unmapped* | - | Ignored. Zomato listing type (e.g., `"Buffet"`, `"Cafes"`, `"Delivery"`). |

---

## 2. Budget Band Calibrations

The system categorizes restaurants into three enums: `LOW`, `MEDIUM`, and `HIGH`. Based on the cost distributions in Indian Metros (specifically Bangalore/Delhi), the bands are calibrated with the following definitions in [RecommendationProperties](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/architecture.md#71-component-catalog):

| Budget Band | INR Range | Rationale | Target Establishments |
| :--- | :--- | :--- | :--- |
| **LOW** | `₹0 - ₹500` | Pocket-friendly snacks, quick bites, small cafes, and traditional breakfasts. | Quick Bites (e.g. Udupi Bhojana), bakeries, tea stalls. |
| **MEDIUM** | `₹501 - ₹1500` | Mid-range casual dining, family restaurants, standard pub visits. | Casual Dining (e.g. Jalsa, Onesta Pizza), standard buffet lines. |
| **HIGH** | `₹1501+` | Premium fine dining, luxury buffets, gourmet experiences. | Five-star dining, microbreweries, high-end thematic bars. |

---

## 3. Metadata Reference (Bangalore/Dev Sample)

For front-end UI dropdown mapping, the top cities and cuisines present in the development sample CSV are:
* **Top Cities**: Banashankari, Basavanagudi, Jayanagar
* **Top Cuisines**: North Indian, South Indian, Chinese, Italian, Continental, Cafe, Mexican, Fast Food
