# Terminology

The words the calculators are allowed to use, per language.

## Why this file exists

The app is translated into twelve languages, and in ten of them nobody on the
project can read the result. That makes the failure mode *plausible*
mistranslation rather than obvious mistranslation: a term that reads fluently
and is not what the trade writes. "Voltage drop" rendered as a word meaning
"voltage loss" is understood; a cross-section labelled as a diameter is acted
on.

So the terms that repeat across the calculators are fixed here, checked against
each country's own installation standard rather than against a dictionary, and
every translation is expected to use them. A term absent from this table is a
term nobody has checked.

## How these were checked

Each language was verified against the wiring standard its electricians
actually work to, not against IEC 60050 alone — IEC's own multilingual entries
are behind a wall this repository cannot reach, and national usage diverges from
them in places where the national word is the right one.

| Language | Checked against |
|---|---|
| German | DIN VDE 0100-520, DIN VDE 0298-4 |
| Spanish | REBT, ITC-BT-19 |
| French | NF C 15-100 |
| Italian | CEI 64-8 |
| Dutch | NEN 1010 |
| Polish | PN-HD 60364-5-52 |
| Portuguese | RTIEBT |
| Russian | ПУЭ, ГОСТ Р 50571.5.52 |
| Vietnamese | TCVN 9206 |
| Indonesian | PUIL 2011 |

Six of the first translations were wrong against these and were corrected:
German `Schutzorgan` → `Schutzeinrichtung`, Spanish `corriente de diseño` →
`corriente de empleo`, Portuguese `capacidade de corrente` → `corrente
admissível`, Vietnamese `khả năng tải dòng` → `dòng điện cho phép`, Indonesian
`drop tegangan` → `susut tegangan`, Dutch `kring` → `stroomkring`.

## The table

| English | de | es | fr | it |
|---|---|---|---|---|
| voltage drop | Spannungsfall | caída de tensión | chute de tension | caduta di tensione |
| cross-section | Querschnitt | sección | section | sezione |
| current-carrying capacity (Iz) | Strombelastbarkeit | intensidad admisible | courant admissible | portata |
| design current (Ib) | Betriebsstrom | corriente de empleo | courant d'emploi | corrente d'impiego |
| rated current of device (In) | Bemessungsstrom | intensidad nominal | calibre | corrente nominale |
| protective device | Schutzeinrichtung | dispositivo de protección | dispositif de protection | dispositivo di protezione |
| protective conductor | Schutzleiter | conductor de protección | conducteur de protection | conduttore di protezione |
| earth fault loop | Fehlerschleife | bucle de defecto a tierra | boucle de défaut | anello di guasto |
| circuit | Stromkreis | circuito | circuit | circuito |
| conductor | Leiter | conductor | conducteur | conduttore |
| installation method | Verlegeart | método de instalación | mode de pose | tipo di posa |
| correction factor | Umrechnungsfaktor | factor de corrección | facteur de correction | fattore di correzione |
| ambient temperature | Umgebungstemperatur | temperatura ambiente | température ambiante | temperatura ambiente |
| short-circuit current | Kurzschlussstrom | corriente de cortocircuito | courant de court-circuit | corrente di cortocircuito |
| power factor | Leistungsfaktor | factor de potencia | facteur de puissance | fattore di potenza |
| XLPE | VPE | XLPE | PR | XLPE |
| RCD | RCD | diferencial | DDR | differenziale |

| English | nl | pl | pt | vi | ru | id |
|---|---|---|---|---|---|---|
| voltage drop | spanningsval | spadek napięcia | queda de tensão | sụt áp | потеря напряжения | susut tegangan |
| cross-section | doorsnede | przekrój | secção | tiết diện | сечение | luas penampang |
| current-carrying capacity (Iz) | stroombelastbaarheid | obciążalność prądowa | corrente admissível | dòng điện cho phép | длительно допустимый ток | kemampuan hantar arus |
| design current (Ib) | ontwerpstroom | prąd obliczeniowy | corrente de serviço | dòng điện tính toán | расчётный ток | arus desain |
| rated current of device (In) | nominale stroom | prąd znamionowy zabezpieczenia | corrente estipulada | dòng định mức | номинальный ток | arus pengenal |
| protective device | beveiliging | zabezpieczenie | dispositivo de proteção | thiết bị bảo vệ | аппарат защиты | gawai proteksi |
| protective conductor | beschermingsleiding | przewód ochronny | condutor de proteção | dây bảo vệ | защитный проводник | penghantar proteksi |
| earth fault loop | aardfoutlus | pętla zwarcia | anel de defeito | vòng sự cố chạm đất | петля фаза-нуль | impedansi gangguan bumi |
| circuit | stroomkring | obwód | circuito | mạch | линия | rangkaian |
| conductor | geleider | przewód | condutor | dây dẫn | проводник | penghantar |
| installation method | installatiemethode | sposób ułożenia | método de instalação | phương pháp lắp đặt | способ прокладки | metode pemasangan |
| correction factor | correctiefactor | współczynnik poprawkowy | fator de correção | hệ số hiệu chỉnh | поправочный коэффициент | faktor koreksi |
| ambient temperature | omgevingstemperatuur | temperatura otoczenia | temperatura ambiente | nhiệt độ môi trường | температура окружающей среды | suhu ambien |
| short-circuit current | kortsluitstroom | prąd zwarciowy | corrente de curto-circuito | dòng ngắn mạch | ток короткого замыкания | arus hubung pendek |
| power factor | arbeidsfactor | współczynnik mocy | fator de potência | hệ số công suất | коэффициент мощности | faktor daya |
| XLPE | XLPE | XLPE | XLPE | XLPE | СПЭ | XLPE |
| RCD | aardlekschakelaar | wyłącznik różnicowoprądowy | diferencial | RCD | УЗО | GPAS |

## What is deliberately not translated

`DC`, `AC`, `AWG`, `IP`, `IK`, and the symbols `Ib`, `In`, `Iz`, `Zs`, `R₁+R₂`,
`cos φ`. An electrician reads and writes these unchanged in every language here,
and localising them makes a string harder to read, not easier. Turkish
`DA`/`AA` is the one form the tests actively forbid — see
`StringResourceIntegrityTest`.

The Romance languages are the exception worth naming: `CC` and `CA` are the
native standard forms for DC and AC and are correct there, which is why the
rule is a per-locale table rather than a blanket ban.

## Units, and the symbols in a formula

Neither is a translator's choice, because neither is drawn from a string.

A unit beside a field or a result comes from Kotlin — `unit = "kW"` — and reads
the same in every language. A string that spells a unit out next to a number
has to spell it that way too, or one quantity ends up with two names a few
millimetres apart: a result card reading `5,30 kW` above the line it exports as
`5,30 кВт`. Russian is where this bites, since its own standard forms are
Cyrillic; the app uses the international symbols throughout, which
ГОСТ 8.417 also permits.

Time is the exception. Seconds, hours and minutes are words in most languages,
and Turkish `günde 8 saat` or a countdown reading `30 sn sonra` is right as it
stands — so the test that enforces the rest leaves time alone.

Formula symbols are the same story from the other side. A formula card takes
the formula from a string and the legend beside it from Kotlin —
`FormulaVariable("P_in", stringResource(mt_var_pin))`. Rendering `P_in` as
`P_zu` in German leaves the legend explaining a letter that is no longer on the
screen. Both rules are tests in `StringResourceIntegrityTest`, and both read
the vocabulary out of the screens rather than keeping a list of their own.
