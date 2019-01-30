# ReadME RoTex Corpus Builder
Builds a corpus of Romanian text from different online sources.

# Description
This project aims to make available, in a transparent way, a high quality corpus delivered as simple text
that can be used for NLP. 
The transformations on original source are as non invasive as possible, 
for example corrections are applied for cedilla diacritics, line stitching for some PDF files 
that breaks the paragraphs too much, but otherwise the text is kept in the original form. 
Some texts are removed, for example the repetitive text from header, footer of PDF files, page numbers, etc. 
Given the non-structured nature of PDF files the results may vary.

_Warning: Please note that not all sources are public domain 
and securing usage rights might be necessary in some cases._  

# How it works
This tool *automatically* downloads, extracts, cleans text and builds the resulting text archives. 

The build process consist of 3 main steps:

- *Download* - The sources are downloaded and saved locally in `original` folder. 
This folder keeps the sources as original files (PDF, epub, etc.) or as close as possible to the original 
(for html sources). The download is not always optimized for speed in order to threat gently the source 
download servers.
- *Extract* - The text is extracted from the original files and saved as a text file in `text` folder. 
All text corrections and transformations are done in this step. For some sources, PDF OCR is applied before text 
extraction, in some cases overriding the low quality one available from the source, taking advantage of the 
Tesseract 4 improvements.
- *Compress* - The text is compressed as a .tar.gz file and saved in `text-compressed` folder.

Running the complete build pipeline for all sources took more than 2 weeks on a 2.9 GHz i7 with 16 GB RAM, the 
main time consumers being the download (especially with many small items) and PDF OCR processes.

The built corpus is available for download as .tar.gz files for each individual source 
and as a single big file containing the entire text.

# Sources
The following is the automatically generated list of all sources ordered by total word count in document:

# To Test
WikiRoSource
JustSource

# TODO

Below are sources that will be considered for inclusion:

http://gazetadecluj.ro/stiri/stiri-cluj/

https://uzp.org.ro/

https://rmj.com.ro/rmj-vol-lvi-nr-3-an-2009/

http://www.tion.ro/date/2019

https://uzp.org.ro/

http://artapolitica.ro/

https://bunavestire.ca/revista-candela/

http://www.certitudinea.ro/

http://www.napocanews.ro/

http://colegiulasachi.uv.ro/scolara.html

http://www.romlit.ro/index.pl/arhiva_2018_ro

https://www.balcanii.ro/2018/11/

https://radiojurnalspiritual.ro/carti-alese/

http://www.umft.ro/carti-in-format-electronic--medicina-generala_184

http://www.dacoromania.inst-puscariu.ro/

https://uituculblog.wordpress.com/citeste-online/carti-pdf/

http://www.cnaa.md/theses/

https://www.jurisprudenta.com/jurisprudenta/

https://eur-lex.europa.eu/homepage.html

http://www.ceeol.com/


http://romania-inedit.3xforum.ro/topic/83/Carti_in_limba_romana/

# Discarded Sources

Below are sources that were considered for inclusion but rejected for various reasons:

- https://biblioteca.regielive.ro/ (very low quality and structure)
- http://bjconstanta.ro/resurse-digitale/carte-romaneasca/ (low quality, few text)
- http://www.elefant.ro/list/ebooks/fictiune/literatura-romana/literatura-romana-clasica?filterprice=0+-+0 (already in bestellermd)
- http://www.bibnat.ro/Biblioteca-Digitala-Nationala-s135-ro.htm (too old)
- http://www.respiro.org/ebook.html (only a few in Romanian, fragmented)

