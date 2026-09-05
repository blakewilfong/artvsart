# Historical event catalog

The game has 106 dated events. Existing enum identifiers are retained so saved questions keep their meaning. Dates describe the named milestone, not an entire multi-year event.

## Familiarity progression

Familiarity is an editorial estimate for a general audience, not historical importance, a humor rating, or a claim that all cultures share the same reference points.

- **Familiar:** widely taught landmarks, famous journeys, or major cultural milestones.
- **Recognizable:** named stories and regional landmarks that many players may recognize but not date precisely.
- **Obscure:** local incidents, specialist history, or stories requiring additional context.

| Round | Familiar | Recognizable | Obscure |
| --- | ---: | ---: | ---: |
| 1 | 85% | 14% | 1% |
| 10 | 50% | 45% | 5% |
| 20 | 15% | 55% | 30% |
| 30 and later | 5% | 25% | 70% |

Weights interpolate linearly between guideposts. There are no tier gates at rounds 11 or 21. These are conditional selection weights, not guaranteed frequencies across all questions: artwork dates, complete lifespans, and date-difficulty requirements first determine eligible events. Empty tiers are omitted and remaining weights renormalized. Within a tier, eligible events have equal weight, so a larger tier cannot overwhelm a smaller one.

Selection uses a symmetric artwork-pair seed and round number. Eligibility, event parameter, and correct answer therefore agree across repeated calls and swapped artwork order. Saved questions retain their stored event and answer. Both event strategies use this policy; other question types keep their existing difficulty rules.

## Reveal and reading behavior

After answering, the question itself links to Wikipedia in a new tab. Links never appear in unanswered HTML. The final event question is also linked inside the high-score panel, so the overlay cannot block access. Separate explanatory blurbs are not displayed. Humor comes from the history rather than invented claims or jokes about victims.

Where auto-advance is already scheduled (active Wager rounds and Streak's final-answer transition), the reveal provides pause/resume. Focusing or opening the Wikipedia link also pauses navigation. Resume starts a fresh 2.5-second timer. Active Streak's existing manual Next question behavior remains unchanged.

## Chronological references

| Event | Year | Familiarity | Wikipedia |
| --- | ---: | --- | --- |
| the completion of the Domesday Book | 1086 | Recognizable | [Article](https://en.wikipedia.org/wiki/Domesday_Book) |
| the beginning of the First Crusade | 1096 | Familiar | [Article](https://en.wikipedia.org/wiki/First_Crusade) |
| the Battle of Tinchebray | 1106 | Obscure | [Article](https://en.wikipedia.org/wiki/Battle_of_Tinchebray) |
| the founding of the Knights Templar | 1119 | Recognizable | [Article](https://en.wikipedia.org/wiki/Knights_Templar) |
| the signing of the Concordat of Worms | 1122 | Obscure | [Article](https://en.wikipedia.org/wiki/Concordat_of_Worms) |
| Louis VII's accession to the French throne | 1137 | Obscure | [Article](https://en.wikipedia.org/wiki/Louis_VII_of_France) |
| the beginning of the Second Crusade | 1147 | Obscure | [Article](https://en.wikipedia.org/wiki/Second_Crusade) |
| Frederick Barbarossa's election as king of Germany | 1152 | Obscure | [Article](https://en.wikipedia.org/wiki/Frederick_Barbarossa) |
| the beginning of construction on Notre-Dame de Paris | 1163 | Recognizable | [Article](https://en.wikipedia.org/wiki/Notre-Dame_de_Paris) |
| the murder of Thomas Becket | 1170 | Recognizable | [Article](https://en.wikipedia.org/wiki/Thomas_Becket) |
| the Erfurt latrine disaster | 1184 | Obscure | [Article](https://en.wikipedia.org/wiki/Erfurt_latrine_disaster) |
| Saladin's capture of Jerusalem | 1187 | Recognizable | [Article](https://en.wikipedia.org/wiki/Siege_of_Jerusalem_(1187)) |
| the end of the Third Crusade | 1192 | Recognizable | [Article](https://en.wikipedia.org/wiki/Third_Crusade) |
| the Fourth Crusade's sack of Constantinople | 1204 | Recognizable | [Article](https://en.wikipedia.org/wiki/Sack_of_Constantinople) |
| the sealing of Magna Carta | 1215 | Familiar | [Article](https://en.wikipedia.org/wiki/Magna_Carta) |
| the beginning of construction on Amiens Cathedral | 1220 | Obscure | [Article](https://en.wikipedia.org/wiki/Amiens_Cathedral) |
| Ferdinand III's capture of Cordoba | 1236 | Obscure | [Article](https://en.wikipedia.org/wiki/Siege_of_C%C3%B3rdoba_(1236)) |
| the Battle of Mohi | 1241 | Obscure | [Article](https://en.wikipedia.org/wiki/Battle_of_Mohi) |
| the arrival of Henry III's elephant in England | 1255 | Obscure | [Article](https://en.wikipedia.org/wiki/Elephant_of_Henry_III) |
| the Mongol sack of Baghdad | 1258 | Recognizable | [Article](https://en.wikipedia.org/wiki/Siege_of_Baghdad_(1258)) |
| the Battle of Ain Jalut | 1260 | Obscure | [Article](https://en.wikipedia.org/wiki/Battle_of_Ain_Jalut) |
| the beginning of Marco Polo's journey to Asia | 1271 | Familiar | [Article](https://en.wikipedia.org/wiki/Marco_Polo) |
| the Sicilian Vespers uprising | 1282 | Obscure | [Article](https://en.wikipedia.org/wiki/Sicilian_Vespers) |
| the fall of Acre | 1291 | Obscure | [Article](https://en.wikipedia.org/wiki/Siege_of_Acre_(1291)) |
| the arrest of the Knights Templar in France | 1307 | Obscure | [Article](https://en.wikipedia.org/wiki/Trials_of_the_Knights_Templar) |
| the beginning of the Great Famine in Europe | 1315 | Obscure | [Article](https://en.wikipedia.org/wiki/Great_Famine_of_1315%E2%80%931317) |
| Mansa Musa's pilgrimage to Mecca | 1324 | Recognizable | [Article](https://en.wikipedia.org/wiki/Mansa_Musa) |
| the beginning of the Hundred Years' War | 1337 | Familiar | [Article](https://en.wikipedia.org/wiki/Hundred_Years%27_War) |
| the arrival of the Black Death in Europe | 1347 | Familiar | [Article](https://en.wikipedia.org/wiki/Black_Death) |
| the issuance of the Golden Bull | 1356 | Obscure | [Article](https://en.wikipedia.org/wiki/Golden_Bull_of_1356) |
| the establishment of the Ming dynasty | 1368 | Recognizable | [Article](https://en.wikipedia.org/wiki/Ming_dynasty) |
| the beginning of the Western Schism | 1378 | Obscure | [Article](https://en.wikipedia.org/wiki/Western_Schism) |
| the Peasants' Revolt in England | 1381 | Recognizable | [Article](https://en.wikipedia.org/wiki/Peasants%27_Revolt) |
| the founding of the Joseon dynasty | 1392 | Obscure | [Article](https://en.wikipedia.org/wiki/Joseon) |
| the Bal des Ardents | 1393 | Obscure | [Article](https://en.wikipedia.org/wiki/Bal_des_Ardents) |
| the beginning of Zheng He's first voyage | 1405 | Recognizable | [Article](https://en.wikipedia.org/wiki/Ming_treasure_voyages) |
| the arrival of a giraffe at the Ming court | 1414 | Obscure | [Article](https://en.wikipedia.org/wiki/Qilin) |
| the Battle of Agincourt | 1415 | Recognizable | [Article](https://en.wikipedia.org/wiki/Battle_of_Agincourt) |
| Joan of Arc's lifting of the siege of Orleans | 1429 | Recognizable | [Article](https://en.wikipedia.org/wiki/Siege_of_Orl%C3%A9ans) |
| Cosimo de' Medici's return to power in Florence | 1434 | Recognizable | [Article](https://en.wikipedia.org/wiki/Cosimo_de%27_Medici) |
| the Battle of Varna | 1444 | Obscure | [Article](https://en.wikipedia.org/wiki/Battle_of_Varna) |
| the fall of Constantinople | 1453 | Familiar | [Article](https://en.wikipedia.org/wiki/Fall_of_Constantinople) |
| the marriage of Ferdinand of Aragon and Isabella of Castile | 1469 | Obscure | [Article](https://en.wikipedia.org/wiki/Ferdinand_II_of_Aragon) |
| the establishment of the Spanish Inquisition | 1478 | Recognizable | [Article](https://en.wikipedia.org/wiki/Spanish_Inquisition) |
| Bartolomeu Dias's rounding of the Cape of Good Hope | 1488 | Recognizable | [Article](https://en.wikipedia.org/wiki/Bartolomeu_Dias) |
| Columbus's arrival in the Americas | 1492 | Familiar | [Article](https://en.wikipedia.org/wiki/Voyages_of_Christopher_Columbus) |
| the establishment of the Safavid dynasty | 1501 | Obscure | [Article](https://en.wikipedia.org/wiki/Safavid_Iran) |
| the creation of Durer's rhinoceros woodcut | 1515 | Obscure | [Article](https://en.wikipedia.org/wiki/D%C3%BCrer%27s_Rhinoceros) |
| the beginning of the Protestant Reformation | 1517 | Familiar | [Article](https://en.wikipedia.org/wiki/Reformation) |
| the Strasbourg dancing plague | 1518 | Recognizable | [Article](https://en.wikipedia.org/wiki/Dancing_plague_of_1518) |
| the fall of the Aztec Empire | 1521 | Familiar | [Article](https://en.wikipedia.org/wiki/Fall_of_Tenochtitlan) |
| the passage of the English Act of Supremacy | 1534 | Obscure | [Article](https://en.wikipedia.org/wiki/Acts_of_Supremacy) |
| Copernicus's publication of his heliocentric model | 1543 | Recognizable | [Article](https://en.wikipedia.org/wiki/De_revolutionibus_orbium_coelestium) |
| the signing of the Peace of Augsburg | 1555 | Obscure | [Article](https://en.wikipedia.org/wiki/Peace_of_Augsburg) |
| the Great Siege of Malta | 1565 | Obscure | [Article](https://en.wikipedia.org/wiki/Great_Siege_of_Malta) |
| the Battle of Lepanto | 1571 | Obscure | [Article](https://en.wikipedia.org/wiki/Battle_of_Lepanto) |
| the defeat of the Spanish Armada | 1588 | Familiar | [Article](https://en.wikipedia.org/wiki/Spanish_Armada) |
| the issuance of the Edict of Nantes | 1598 | Obscure | [Article](https://en.wikipedia.org/wiki/Edict_of_Nantes) |
| the founding of Jamestown | 1607 | Recognizable | [Article](https://en.wikipedia.org/wiki/Jamestown%2C_Virginia) |
| the beginning of the Thirty Years' War | 1618 | Recognizable | [Article](https://en.wikipedia.org/wiki/Thirty_Years%27_War) |
| the Mayflower's arrival at Plymouth | 1620 | Familiar | [Article](https://en.wikipedia.org/wiki/Mayflower) |
| the sinking of the Vasa on its maiden voyage | 1628 | Recognizable | [Article](https://en.wikipedia.org/wiki/Vasa_(ship)) |
| Galileo's trial by the Roman Inquisition | 1633 | Recognizable | [Article](https://en.wikipedia.org/wiki/Galileo_affair) |
| the collapse of the Dutch tulip market | 1637 | Recognizable | [Article](https://en.wikipedia.org/wiki/Tulip_mania) |
| the signing of the Peace of Westphalia | 1648 | Obscure | [Article](https://en.wikipedia.org/wiki/Peace_of_Westphalia) |
| the publication of Hobbes's Leviathan | 1651 | Obscure | [Article](https://en.wikipedia.org/wiki/Leviathan_(Hobbes_book)) |
| the Great Fire of London | 1666 | Familiar | [Article](https://en.wikipedia.org/wiki/Great_Fire_of_London) |
| the beginning of the Franco-Dutch War | 1672 | Obscure | [Article](https://en.wikipedia.org/wiki/Franco-Dutch_War) |
| the publication of the Women's Petition Against Coffee | 1674 | Obscure | [Article](https://en.wikipedia.org/wiki/History_of_coffee) |
| the publication of Newton's Principia | 1687 | Recognizable | [Article](https://en.wikipedia.org/wiki/Philosophi%C3%A6_Naturalis_Principia_Mathematica) |
| the Salem witch trials | 1692 | Familiar | [Article](https://en.wikipedia.org/wiki/Salem_witch_trials) |
| the Acts of Union that created Great Britain | 1707 | Recognizable | [Article](https://en.wikipedia.org/wiki/Acts_of_Union_1707) |
| the Jacobite rising of 1715 | 1715 | Obscure | [Article](https://en.wikipedia.org/wiki/Jacobite_rising_of_1715) |
| the Treaty of Nystad ending the Great Northern War | 1721 | Obscure | [Article](https://en.wikipedia.org/wiki/Treaty_of_Nystad) |
| Mary Toft's rabbit birth hoax | 1726 | Obscure | [Article](https://en.wikipedia.org/wiki/Mary_Toft) |
| the patenting of the flying shuttle | 1733 | Obscure | [Article](https://en.wikipedia.org/wiki/Flying_shuttle) |
| the beginning of the War of the Austrian Succession | 1740 | Obscure | [Article](https://en.wikipedia.org/wiki/War_of_the_Austrian_Succession) |
| the beginning of the Seven Years' War | 1756 | Recognizable | [Article](https://en.wikipedia.org/wiki/Seven_Years%27_War) |
| the passage of the Stamp Act | 1765 | Recognizable | [Article](https://en.wikipedia.org/wiki/Stamp_Act_1765) |
| the observation of Venus crossing the Sun from Tahiti | 1769 | Obscure | [Article](https://en.wikipedia.org/wiki/1769_transit_of_Venus_observed_from_Tahiti) |
| the adoption of the American Declaration of Independence | 1776 | Familiar | [Article](https://en.wikipedia.org/wiki/United_States_Declaration_of_Independence) |
| the Montgolfiers' balloon flight with animal passengers | 1783 | Recognizable | [Article](https://en.wikipedia.org/wiki/Montgolfier_brothers) |
| the beginning of the French Revolution | 1789 | Familiar | [Article](https://en.wikipedia.org/wiki/French_Revolution) |
| the beginning of the Haitian Revolution | 1791 | Recognizable | [Article](https://en.wikipedia.org/wiki/Haitian_Revolution) |
| the rediscovery of the Rosetta Stone | 1799 | Recognizable | [Article](https://en.wikipedia.org/wiki/Rosetta_Stone) |
| Haiti's declaration of independence | 1804 | Recognizable | [Article](https://en.wikipedia.org/wiki/Haitian_Declaration_of_Independence) |
| the London Beer Flood | 1814 | Obscure | [Article](https://en.wikipedia.org/wiki/London_Beer_Flood) |
| the Battle of Waterloo | 1815 | Familiar | [Article](https://en.wikipedia.org/wiki/Battle_of_Waterloo) |
| the beginning of the Greek War of Independence | 1821 | Obscure | [Article](https://en.wikipedia.org/wiki/Greek_War_of_Independence) |
| the arrival of a giraffe in Paris | 1827 | Obscure | [Article](https://en.wikipedia.org/wiki/Zarafa_(giraffe)) |
| the July Revolution in France | 1830 | Obscure | [Article](https://en.wikipedia.org/wiki/July_Revolution) |
| the Great Moon Hoax | 1835 | Recognizable | [Article](https://en.wikipedia.org/wiki/Great_Moon_Hoax) |
| the Revolutions of 1848 | 1848 | Recognizable | [Article](https://en.wikipedia.org/wiki/Revolutions_of_1848) |
| London's Great Stink | 1858 | Recognizable | [Article](https://en.wikipedia.org/wiki/Great_Stink) |
| the publication of Darwin's On the Origin of Species | 1859 | Familiar | [Article](https://en.wikipedia.org/wiki/On_the_Origin_of_Species) |
| the beginning of the American Civil War | 1861 | Familiar | [Article](https://en.wikipedia.org/wiki/American_Civil_War) |
| the opening of the first Impressionist exhibition | 1874 | Recognizable | [Article](https://en.wikipedia.org/wiki/First_Impressionist_Exhibition) |
| the opening of the Eiffel Tower | 1889 | Familiar | [Article](https://en.wikipedia.org/wiki/Eiffel_Tower) |
| women's first parliamentary vote in New Zealand | 1893 | Recognizable | [Article](https://en.wikipedia.org/wiki/Women%27s_suffrage_in_New_Zealand) |
| the first modern Olympic Games | 1896 | Familiar | [Article](https://en.wikipedia.org/wiki/1896_Summer_Olympics) |
| the Wright Flyer's first flight | 1903 | Familiar | [Article](https://en.wikipedia.org/wiki/Wright_Flyer) |
| the first Peking to Paris motor race | 1907 | Obscure | [Article](https://en.wikipedia.org/wiki/Peking_to_Paris) |
| the theft of the Mona Lisa from the Louvre | 1911 | Recognizable | [Article](https://en.wikipedia.org/wiki/Mona_Lisa) |
| the beginning of the First World War | 1914 | Familiar | [Article](https://en.wikipedia.org/wiki/World_War_I) |
| the discovery of Tutankhamun's tomb | 1922 | Familiar | [Article](https://en.wikipedia.org/wiki/Discovery_of_the_tomb_of_Tutankhamun) |
| Australia's Emu War | 1932 | Recognizable | [Article](https://en.wikipedia.org/wiki/Emu_War) |

## Research notes

New entries were checked against their linked Wikipedia articles. Additional corroboration and useful context:

- [UK Parliament on the Great Stink](https://www.parliament.uk/about/living-heritage/building/palace/estatehistory/from-the-parliamentary-collections/thames/estimatethamespurification/) supports disruption of parliamentary business in 1858; the game does not claim a formal evacuation.
- [The Metropolitan Museum of Art on the Ming giraffe](https://www.metmuseum.org/art/collection/search/60509) supports the 1414 gift and the qilin association.
- [The Eiffel Tower's official history of the controversy](https://www.toureiffel.paris/en/news/history-and-culture/when-eiffel-tower-was-subject-controversy) supports the artists' protest and subsequent popularity.
- [National Park Service on the Wright Flyer](https://www.nps.gov/articles/wrightflyer.htm) supports the first flight's twelve-second duration and 120-foot distance.
- The Strasbourg dancing plague's causes and death toll are disputed; no exact death count is asserted.
- Tulip mania is not described as having ruined the entire Dutch economy.
- The Emu War is identified as a wildlife-control operation, not a literal interstate war.

## Verification

Run the Java suite with `.\\mvnw.cmd test`. Run navigation tests with `node --test src/test/js/game.test.cjs`. To generate local rendered-template previews in the ignored target directory, run `.\\mvnw.cmd -Dtest=EventRevealTemplateTest -Dartvsart.preview=true test`.
