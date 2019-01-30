# ReadME RoTex Corpus Builder
Builds a corpus of Romanian text from different online sources.

# Description
This project aims to make available, in an open and transparent way, a high quality corpus of Romanian texts 
that can be used for NLP. 
The transformations on original source are as non invasive as possible, 
for example corrections are applied for cedilla diacritics, line stitching for some PDF files 
that breaks the paragraphs too much, but otherwise the text is kept in the original form. 
Some texts are removed, for example the repetitive text from header, footer of PDF files, page numbers, etc. 
Given the non-structured nature of PDF files the results may vary.

_*Warning:* Please note that not all sources are public domain 
and securing usage rights might be necessary in some cases._  

# How it works
This tool *automatically* downloads, extracts, cleans and assemble the resulting text archives. 

The build process has 3 main steps:

- *Download* - The sources are downloaded and saved locally in `original` folder. 
This folder keeps the sources as original files (PDF, epub, etc.) or as close as possible to the original 
(for html sources). The download is not always optimized for speed in order to threat gently the source 
download servers.
- *Extract* - The text is extracted from the original files and saved as a text file in `text` folder. 
All text corrections and transformations are done in this step. Example of corrections are cedilla diacritics 
replacement, restoring PDF font mappings, stripping multiple blank lines, etc.. 
For some sources, OCR is applied before text extraction even if it exists in the original source document, 
to override the low quality one available from the source, taking advantage of Tesseract 4 improvements.
- *Compress* - The text is compressed as a .tar.gz file and saved in `text-compressed` folder.

It is possible to incrementally run the pipeline, by default the already completed steps are skipped. 
Running the complete build pipeline for all sources took more than 2 weeks on a 2.9 GHz i7 with 16 GB RAM, the 
main time consumers being the download (especially with many small items) and PDF OCR processes.

The built corpus is available for download as .tar.gz files for each individual source 
and as a single big file containing the entire text.

# Sources

Sources ordered by total word count in document:w

Source | Word Count | Types Count | DEX Coverage | Uncompressed size | Compressed size
--- | --- | --- | --- | --- | ---
biblior [➭](http://biblior.net/carti "View source site") | 1.181.820 | 89.642 | 5,9% (72.298) | 7 MB | 2 MB [▼Download]( "Download compressed file")
carti-bune-gratis [➭](http://cartibunegratis.blogspot.ro "View source site") | 1.619.354 | 76.103 | 5,54% (67.833) | 9 MB | 3 MB [▼Download]( "Download compressed file")
historica-cluj [➭](http://www.historica-cluj.ro/menu/arhiva_anuar.php "View source site") | 2.542.106 | 150.298 | 6,55% (80.157) | 18 MB | 6 MB [▼Download]( "Download compressed file")
destine-literale [➭](http://www.scriitoriiromani.com/DestineLiterare.html "View source site") | 4.325.392 | 270.410 | 11,29% (138.233) | 27 MB | 11 MB [▼Download]( "Download compressed file")
paul-goma [➭](http://www.paulgoma.com/lista-completa "View source site") | 6.603.181 | 254.228 | 10,82% (132.469) | 42 MB | 16 MB [▼Download]( "Download compressed file")
rudolf-steiner [➭](http://www.spiritualrs.net/Lucrari_GA.html "View source site") | 7.678.761 | 106.878 | 5,52% (67.549) | 50 MB | 15 MB [▼Download]( "Download compressed file")
litera-net [➭](http://editura.liternet.ro/catalog/1/Romana/toate-cartile.html "View source site") | 8.591.552 | 263.844 | 14,88% (182.250) | 54 MB | 21 MB [▼Download]( "Download compressed file")
biblioteca-digitala-ase [➭](http://www.biblioteca-digitala.ase.ro/biblioteca "View source site") | 16.105.049 | 256.196 | 10,73% (131.383) | 121 MB | 37 MB [▼Download]( "Download compressed file")
biblioteca-pe-mobil [➭](https://scoala.bibliotecapemobil.ro "View source site") | 19.299.099 | 419.782 | 17,09% (209.309) | 116 MB | 44 MB [▼Download]( "Download compressed file")
ziarul-lumina [➭](http://ziarullumina.ro "View source site") | 23.693.901 | 271.607 | 13,17% (161.249) | 168 MB | 59 MB [▼Download]( "Download compressed file")
bestseller-md [➭](https://www.bestseller.md "View source site") | 27.766.289 | 348.555 | 18,01% (220.517) | 171 MB | 63 MB [▼Download](https://drive.google.com/open?id=1PvUXcnvPo6dOyA8L6lCuqbsAvlWp3BeV&export=download "Download compressed file")
dcep [➭](https://wt-public.emm4u.eu/Resources/DCEP-2013/DCEP-Download-Page.html "View source site") | 34.534.679 | 174.371 | 6,75% (82.655) | 262 MB | 71 MB [▼Download]( "Download compressed file")
bzi [➭](https://www.bzi.ro/arhiva "View source site") | 42.923.167 | 289.744 | 13,96% (170.975) | 301 MB | 105 MB [▼Download]( "Download compressed file")
ru-101-books [➭](http://www.101books.ru/ "View source site") | 87.936.969 | 706.772 | 24,83% (303.991) | 534 MB | 199 MB [▼Download]( "Download compressed file")
dezbateri-parlamentare [➭](http://www.cdep.ro/pls/steno/steno.home?idl=1 "View source site") | 109.244.724 | 250.406 | 14,22% (174.140) | 764 MB | 227 MB [▼Download]( "Download compressed file")
just [➭](http://legislatie.just.ro/Public/RezultateCautare?page=1 "View source site") | 188.155.635 | 580.225 | 20,16% (246.794) | 1.998 MB | 349 MB [▼Download]( "Download compressed file")
wiki-ro [➭](https://dumps.wikimedia.org/rowiki/latest/rowiki-latest-pages-meta-current.xml.bz2 "View source site") | 198.707.897 | 2.429.146 | 40,85% (500.213) | 1.441 MB | 341 MB [▼Download]( "Download compressed file")
all [➭](https://github.com/aleris/ReadME-RoTex-Corpus-Builder "View source site") | 780.909.575 | 3.687.549 | 58,3% (713.796) | 6.090 MB | 1.578 MB [▼Download]( "Download compressed file")

(¹) Total number of words in the source, where a word is considered any sequence of letters, even if it is not present 
in DEX.

(²) Total number of types in the source, or unique words. Theoretically this should be under the number of word forms 
in DEX, however in some cases, where the source has fragmented sections with words from other languages, 
like in wikipedia the number can be higher. 

(³) A percentage of words covered from the source from the total word forms in DEX. For example DEX has 
approximately 1.2 millions word forms and if in the source we have 130.000 unique words then the coverage is
about 11%.

# TODO

Sources to be considered for inclusion:

* http://gazetadecluj.ro/stiri/stiri-cluj/
* https://uzp.org.ro/
* https://rmj.com.ro/rmj-vol-lvi-nr-3-an-2009/
* http://www.tion.ro/date/2019
* https://uzp.org.ro/
* http://artapolitica.ro/
* https://bunavestire.ca/revista-candela/
* http://www.certitudinea.ro/
* http://www.napocanews.ro/
* http://colegiulasachi.uv.ro/scolara.html
* http://www.romlit.ro/index.pl/arhiva_2018_ro
* https://www.balcanii.ro/2018/11/
* https://radiojurnalspiritual.ro/carti-alese/
* http://www.umft.ro/carti-in-format-electronic--medicina-generala_184
* http://www.dacoromania.inst-puscariu.ro/
* https://uituculblog.wordpress.com/citeste-online/carti-pdf/
* http://www.cnaa.md/theses/
* https://www.jurisprudenta.com/jurisprudenta/
* https://eur-lex.europa.eu/homepage.html
* http://www.ceeol.com/
* http://romania-inedit.3xforum.ro/topic/83/Carti_in_limba_romana/

# Discarded Sources

Below are sources that were considered for inclusion but rejected for various reasons:

- https://biblioteca.regielive.ro/ (very low quality and structure)
- http://bjconstanta.ro/resurse-digitale/carte-romaneasca/ (low quality, few text)
- http://www.elefant.ro/list/ebooks/fictiune/literatura-romana/literatura-romana-clasica?filterprice=0+-+0 (already in bestellermd)
- http://www.bibnat.ro/Biblioteca-Digitala-Nationala-s135-ro.htm (too old)
- http://www.respiro.org/ebook.html (only a few in Romanian, fragmented)
- http://www.cimec.ro/Biblioteca-Digitala/Biblioteca.html (too many images, very fragmented text)

