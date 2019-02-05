package ro.readme.rotex.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.FileReader

internal class BufferedSentenceReaderTest {

    private val bufferedSentenceDetector = BufferedSentenceReader()

    @Test
    fun read1() {
        val exampleString =
                "Da este. R. D. este."
        test(exampleString, 2)
    }

    @Test
    fun read2() {
        val exampleString =
                "Este în sală? Este în sală?!?"
        test(exampleString, 2)
    }

    @Test
    fun read3() {
        val exampleString =
                "Dacă este, dr. Mihai Dumitriu!!!"
        test(exampleString, 1)
    }

    @Test
    fun read4() {
        val exampleString =
                "\"Veacul nostru ni-l umplură, saltimbancii şi irozii...\" Drept pentru care oftez şi eu precum, odinioară, cronicarul: \"Oh, oh, vai di ţarâ...\"."
        test(exampleString, 3)
    }

    @Test
    fun read5() {
        val exampleString =
                "\"Veacul nostru ni-l umplură, saltimbancii şi irozii\". Drept pentru care oftez şi eu precum, odinioară, cronicarul: \"Oh, oh, vai di ţarâ...\"."
        test(exampleString, 3)
    }

    @Test
    fun read6() {
        val exampleString =
                "A fost. De la orele 18:00, la mine. Acum din nou."
        test(exampleString, 3)
    }

    @Test
    fun read7() {
        val exampleString =
                "Mama ei, care torcea după horn, cuprinsă de spaimă, zvârli fusul din mână și furca din brâu cât colo și, sărind fără sine, o întrebă cu spaimă:\n" +
                        "— Ce ai, draga mamei, ce-ți este?!\n" +
                        "— Mamă, mamă! Copilul meu are să moară!"
        test(exampleString, 4)
    }

    @Test
    fun read8() {
        val exampleString =
                "În valoare de 196.247 lei noi. S-au constatat 79 de infracţiuni."
        test(exampleString, 2)
    }

    @Test
    fun read9() {
        val exampleString =
                "Povestiri\n" +
                        "proză\n" +
                        "Prostia omenească"
        test(exampleString, 2)
    }

    @Test
    fun read10() {
        val exampleString =
                "În prezență se atrag sau se resping etc. Ați înțeles lucrul acesta?"
        test(exampleString, 2)
    }

    @Test
    fun readArtificialComplex() {
        val exampleString =
                "Domnul Corneliu Ciontu: Îl invit la microfon (pe domnul) Paul Magheru de la M.A.P.N., ș.a.m.d. va urma \n" +
                        "domnul Viorel Pupeză să \"vorbească\" despre art. 89 lit. C) și alin. 8A9, aducând detalii \n" +
                        "suplimentare. Nu. Deci nu...Dacă este, dr. Mihai Dumitriu!!! Domnul deputat Ştefan la 12. \n" +
                        "ian. 2018 Baban, aparţinând «Grupului parlamentar» al P. R. M., dacă este în sală\n" +
                        "? Este în sală?!? Da este. R. D. este."
        test(exampleString, 9)
    }

    @Test
    fun readReal1() {
        val exampleString =
                "Domnul Costache Mircea: \"Moluştele preşedintelui\" Cine şi-ar fi închipuit că în locul mult huliţilor activişti de partid Ion Teleagă şi Suzana Gâdea ne vom întâlni după revoluţie cu Radu Deltaplanu' şi Anca Boagiu. Vedem azi şi nu ne vine să credem, că activiştii, intoleranţi şi inculţi, erau măcar oameni serioşi, responsabili faţă de producţia de bunuri materiale şi spirituale, de avuţia naţională. Vedem că erau mai eficienţi şi mai patrioţi decât leprele postdecembriste, corupte, imorale şi aculturale. Ar fi curată pierdere de vreme să înşiruim aici nume de scamatori din teatrul de paiaţe al impostorilor de azi. Vă dau numai două exemple: priviţi faţa palidă, de înecat, a la Ion Mohreanu din \"Îngerul a strigat\" de Fănuş Neagu, a lui Radu Berceanu. Şiştavul acesta, căruia i s-a năzărit să fie numărul doi în Stat, ca simbol al halului în care a decăzut şandramaua românească începând cu anul 1990, coboară zilnic dintr-un automobil supranatural, de sorginte extraterestră, valorând vreo jumătate de milion de dolari, face copii cu secretarele şi are o avere de mare magnat. De unde? Cum de unde, din ministeriat. Că acest scopete a fost ministrul CDR-ist al economiei. Ce isprăvi a făcut? Multe, el lua şpagă sub semnătură proprie pe hârtie cu antetul restaurantului \"Athénée Palace\", de 350.000 dolari numai într-o seară, după care, a doua zi, dispunea oprirea furnizării energiei electrice către Basarabia, pentru că, vezi-Doamne, fraţii noştri aveau o datorie către statul român de 25 milioane dolari. Dar datoria noastră către cei de dincolo de Prut, cât valorează, animalule? Dar n-avea cine să-l întrebe, că Ţapul era ocupat cu semnarea Tratatului cu Ucraina ... Şi încă un personaj care cere nu numai palme, ci şi nişte scuipaţi, bine ţintiţi între ochelarii de croitoreasă, Anca Boagiu, care deunăzi, ne-a dovedit că gripa aviară face ravagii printre găinile zăpăcite din grădina Preşedintelui. Una dintre câştigătoarele războiului chiloţilor din PD, care şi-a rentabilizat la maxim gândirea cu ovarele, a ajuns de două ori ministru. Această Pena Corcoduşa a tranziţiei transferă, la comandă, ineficienţa ministerului care o conduce, în seama Parlamentului, prefăcându-se a nu şti că a fost şi este şi acum deputat(ă). Dacă vrea cineva să se convingă că chivuţa asta cu voce de guristă fără taraf, confundă locomotiva cu directiva, s-o închidă singură într-un birou şi să-i dea un extemporal cu următoarele întrebări: Câţi salariaţi are MIE? Câţi, din cei vreo mie, ştiu despre ce e vorba în propoziţie, în afară de Leonard Orban, C. Pleşea şi alţi vreo 20 de consilieri tineri? Care sunt instituţiile Uniunii Europene? Dacă din cele 42 va şti vreo 5, rugaţi-o să-i spună şi lui Traian Băsescu care e diferenţa dintre Consiliul Europei şi Consiliul European. Cum se numesc babetele cu negi pe nas de la Serviciul Protocol al MIE, care îi întâmpină pe diplomaţii statelor comunitare şi pe delegaţii străini cu elevata formulă de politeţe, în limba spargă: \"velcăm, velcăm!\", de-ţi crapă obrazul de ruşine de ruşinea lor. Cu astfel de competenţe şi cu o aşa ministresă ne putem integra, mai degrabă, în absolut, ca guru Bivolaru! Astea se pricep la engleză mai ceva ca traducătorul discursului lui Bush, care ne spunea că \"vom lupta şold la şold\", în loc de \"umăr la umăr\". Vai de mama noastră care ne-a făcut miniştri, care asmut mereu presa pe toţi parlamentarii, inclusiv pe cei cinstiţi, competenţi şi animaţi de duhul naţional, la grămadă. Că bine zicea poetul: \"Veacul nostru ni-l umplură, saltimbancii şi irozii...\" Drept pentru care oftez şi eu precum, odinioară, cronicarul: \"Oh, oh, vai di ţarâ...\".\n" +
                        "Domnul Claudiu Adrian Pop: Declaraţie politică cu privire la abuzurile nedemocratice ale actualei Puteri Stimaţi colegi, Pentru a câta oară în ultimele 9 luni actuala coaliţie aflată la guvernare, prin şantaj, încearcă să modifice voinţa electoratului? Da, domnilor guvernanţi! Este vorba de ŞANTAJ!!! Aţi ajuns să conduceţi ţara prin şantaj şi minciună - strigând şi arătând spre PSD, că am fraudat alegerile generale, dar până acum nu aţi adus nici o dovadă. Ba nu, există o dovadă, un fost candidat ales senator PD a fost dovedit că a fraudat alegerile. Deşi în fiecare zi daţi dovadă de incompetenţă crasă în ce priveşte administrarea ţării, acum doriţi cu orice preţ să modificaţi şi situaţia rezultată după alegerile locale din primăvara anului 2004. Adunaţi pe înserate la Palatul Victoria (văd că aţi renunţat la terasele restaurantelor, pe care le-aţi făcut celebre prin campaniile publicitare gratuite), ameninţaţi cu modificarea Legii nr. 215 a administraţiei locale, printr-o ordonanţă de urgenţă, dacă nu veţi reuşi să modificaţi regulamentele celor două Camere ale Legislativului în termenul de 15 zile. Domnilor, Sunt trimis aici, în Parlamentul României, de către electoratul din judeţul Bihor şi mă simt dator să răspund în faţa celor care m-au trimis aici şi care nu înţeleg cum în această ţară legea se face şi se schimbă după cum vor muşchii preşedintelui Băsescu sau ai celor care au ajuns la guvernare pe uşa din dos. Mă văd nevoit să trag un semnal de alarmă în legătură cu goana după funcţii pe care membrii PNL-PD o manifestă şi de felul în care îşi bat joc de voinţa electoratului. Recent aflat în judeţul Bihor, ministrul Flutur nu a avut nimic mai bun de făcut decât să declare într-o conferinţă de presă că posturile ocupate de membrii PSD în conducerile administraţiilor judeţene trebuie \"operate\" cât mai urgent şi preluate de cei din Alianţa D.A., pentru că altfel ţara nu poate fi guvernată eficient. Cât de eficienţi sunteţi, o dovediţi în fiecare moment! Iar dorinţele ministeriabilului în cauză sunt aberaţii nedemocratice. Îi atragem atenţia domnului Flutur că ţara are legi şi trebuie respectate chiar şi de cei ce vremelnic se află la guvernare. Nu pretind că legile existente nu sunt perfectibile, dar în calitatea mea de ales al acestui popor, la care vă place să faceţi atâta referire, am obligaţia morală să semnalez abuzurile la care vă dedaţi. Vă sugerez să citiţi cu atenţie cartea de căpătâi a democraţiei, după care părinţii noştri şi noi, tinerii din această ţară, am tânjit decenii. Prevederile Constituţiei nu sunt facultative, iar art. 1 pct. 5, care spune că: \"În România, respectarea Constituţiei, a supremaţiei sale şi a legilor este obligatorie\" nu este valabil numai pentru unii. Daţi dovadă, pentru a mia oară, dacă mai era nevoie, de dispreţ faţă de popor! Am început să cred că suferiţi acut de nihilism, pentru că alfel nu pot să-mi explic declaraţiile pe care le-aţi făcut, domnule Flutur şi stimabili domni din conducerea Coaliţiei. Văd că dacă nu puteţi să obţineţi funcţiile dorite prin procesul normal, democratic, al alegerilor, sunteţi capabili să călcaţi în picioare toate principiile statului de drept, pentru a vă atinge scopurile. Am o veste pentru dumneavoastră, cei care acum doriţi să modificaţi o lege organică printr-o ordonanţă de urgenţă: funcţiile ocupate de către reprezentanţii PSD la nivelul consiliilor judeţene au fost şi sunt consfinţite prin vot - o dată prin votul electoratului şi a doua oară prin votul aleşilor în forurile administrative judeţene - aşa cum prevede LEGEA. Nicăieri şi nimeni nu poate dovedi că alegerea lor a fost făcută pe alte criterii decât cele prevăzute de lege, în urma unui vot liber exprimat. Sunt oameni competenţi, fiecare în profesia sa. Au dat dovadă de profesionalism şi bună-credinţă faţă de electorat şi faţă de interesele acestuia, însă au o singură vină: sunt membri ai Partidului Social Democrat. Domnilor guvernanţi, Stimaţi colegi parlamentari, atât din arcul guvernamental, cât şi din Opoziţie, Nu am venit astăzi în faţa dumneavoastră cu alt gând decât acela de a vă spune că a început să-mi fie frică de acţiunile politice ale celor care conduc astăzi România. Mă întreb şi vă întreb pe dumneavoastră, domnilor parlamentari ai Puterii, unii cu mai multă experienţă legislativă, unde doriţi să duceţi ţara asta? Declarativ, spre Uniunea Europeană. Practic, pe zi ce trece, spre totalitarism. Mai ieri, un coleg susţinea de la această tribună că a fost trimis în Parlament pentru a ne dispreţui. Alte voci şi minţi înfierbântate de beţia puterii doresc să ne vadă desfiinţaţi, scoşi de pe scena politică a ţării. Ne arătaţi obrazul că, vezi Doamne, \"am mers cu jalba în proţap\" la forurile europene. Eu, un novice în ale luptelor parlamentare, vă spun, domnilor, că generaţia mea, electoratul nostru, majoritatea populaţiei acestei ţări nu văd în acţiunile dumneavoastră altceva decât O VENDETĂ POLITICĂ. Dacă veţi continua în acest mod, cu siguranţă, integrarea României în Uniunea Europeană va fi compromisă, iar vina o veţi avea doar dumneavoastră, cei care, în loc să guvernaţi, vă ocupaţi cu satisfacerea intereselor şi orgoliilor meschine pe care le aveţi. Vă mulţumesc.\n" +
                        "Domnul Florin Iordache: \"Reforma în justiţie - încălcarea normelor şi principiilor democratice\" Graba cu care Ministerul Justiţiei încearcă să realizeze aşa-zisa reformă este justificată de apropierea datei la care va fi dat publicităţii raportul de ţară. Aşa-zisa reformă se realizează însă prin încălcarea normelor şi principiilor democratice, prin coerciţie şi presiune. Întrega perioadă de la instalarea doamnei Monica Macovei la conducerea Ministerului Justiţiei a fost marcată de o permanentă stare de conflict între Consiliul Superior al Magistraturii, Parchetul General, Înalta Curte de Casaţie şi Justiţie. În prima conferinţă de presă a domnului Daniel Morar, acesta anunţa la TV, în direct, înlocuirea unor procurori, fără a li se cere explicaţii şi acestora. Astfel, prin discreditare publică, se doreşte transformarea PNA într-un instrument care să acţioneze la comanda politcă. Doamna Monica Macovei încearcă să forţeze procurorii să emită rechizitorii în fiecare dosar, să creeze dosare de mare corupţie fostei Puteri, iar judecătorii să condamne la normă. Acest proces care afectează independenţa justiţiei are ca singur scop subordonarea politică a acesteia, dar amestecul politicului nu face decât să încalce principiul separaţiei puterilor în stat.\n" +
                        "Domnul Marin Diaconescu: \"Învăţământul - exclus de mult de pe lista priorităţilor.\" Stimaţi colegi, Aşa cum ne-am obişnuit deja, în fiecare săptămână suntem martorii exploziei de suferinţe pe care o trăiesc locuitorii acestei ţări. După nenumăratele nenorociri care s-au abătut asupra sinistraţilor urmare a calamităţilor, mai mulţi profesori din rândul acestora nu au mai suportat umilinţa generată de lipsa banilor şi au ieşit în stradă. Îmbrăcaţi cu toţii în negru, pentru a simboliza doliul din inimile lor, aceşti oameni au venit în faţa Guvernului pentru a-şi striga durerea. Ei sunt protagoniştii unor adevărate drame şi nimănui nu îi pasă de destinele lor. Nu ştiu câţi dintre noi ar putea trăi dintr-un salariu de profesor. Nu ştiu câţi dintre copiii noştri sunt stimulaţi să îmbrăţişeze o carieră de profesor, ştiind ce viaţă mizeră îi aşteaptă. Cred că ar trebui să ne amintim că de aceşti oameni depinde viitorul copiilor noştri. Dacă ei nu mai depun eforturi pentru a-i aduce pe copiii noştri pe culmi înalte, înseamnă că am pierdut această bătălie cu toţii. Dar acest lucru nu cred că ar cântări prea mult în ochii colegilor noştri de la Putere. Spun aceasta pentru că, aşa cum am urmărit cu toţii pe posturile de televiziune, preşedintele ţării a ieşit la rampă şi a încurajat elevii să nu mai înveţe carte, dacă doresc să devină cândva oameni mari, cu influenţă. Adevărul este că puteam să ghicim faptul că nu i-a plăcut foarte cartea, dacă luam drept reper realizările sale de până acum, precum şi modul de soluţionare a problemelor care necesitau o rezolvare corectă şi punctuală. Când a făcut afirmaţiile acestea, preşedintele a uitat să precizeze imaginea pe care o are România sub conducerea sa: o ţară dominată de instabilitate politică, cu oameni năpăstuiţi de soartă, cu un învăţământ situat la poziţiile ultime ale priorităţilor actualei Puteri şi cu o conducere incapabilă să exceleze în orice domeniu. Adevărul este că oamenii se schimbă mult atunci când pun mâna pe putere. Exemplul cel mai bun este cel al preşedintelui. De când şi-a tăiat şuviţa, face numai ce doreşte domnia sa, ba, mai mult, tot el este cel care decide şi modul de ducere la îndeplinire a sarcinilor, trasate tot de domnia sa. Cum este posibil să încurajăm copiii să nu mai înveţe, ba, chiar mai mult, să nu mai depună toate diligenţele pentru a deveni cei mai buni dintre cei mai buni, pentru că eforturile lor sunt în zadar? Eu, fiind profesor de meserie, mi-am permis să iau cuvântul azi, aici, pentru a reprezenta ecoul vocilor acestor oameni. Ştiu ce înseamnă să stimulezi un copil să înveţe bine, ce înseamnă să îl convingi că ceea ce face este important, nu numai pentru el, ci pentru întreaga lume. Cum să mai dorească aceşti copii să îmbrăţişeze meseria de profesor, când ei văd că învăţământul nu se află pe lista priorităţilor actualei Puteri şi, mai mult decât atât, că se va vorbi din nou de învăţământ ca de o prioritate la următoarele alegeri? Acum, prioritare sunt anticipatele şi schimbarea preşedinţilor celor două Camere, restul rămâne de văzut şi, până va veni acel moment, totul va rămâne cufundat în ceaţă. Vă mulţumesc.\n" +
                        "Domnul Ion Dumitru: \"Ministrul şi ... pompele!\" Este uşor de remarcat cum ministrul agriculturii, alimentaţiei şi pădurilor, Gheorghe Flutur, se chinuieşte din răsputeri să iasă în evidenţă cu ceva. Îşi întinde aripile portocalii, brodate cu săgeţele, peste ogoarele patriei şi fâlfâie fără rost. În vară, înainte de a se porni potopul, ministrul Flutur a ieşit în faţă pentru a lăuda producţia de cereale. Nu s-a gândit nici o clipă (sau poate că s-a gândit!) că vorbele sale vor avea un efect nedorit, dând peste cap preţul la grâu. Nici nu se copsese bine grâul şi, gata, trecem să ne lăudăm cu munca altora. Prudenţa în declaraţii ar trebui să reprezinte scutul oricărui politician adevărat. Uite că Dumnezeu l-a lovit peste mândrie pe Flutur. Ploile au afectat întreaga producţie de cereale. Laudele i-au rămas în gât ministrului. Au venit apoi şi inundaţiile. Şeful peste agricultura românească s-a zbătut să nu iasă nici din această nenorocire cu imaginea şifonată. A umblat cu pompa de evacuat apă prin zonele calamitate. Fără prea mult folos. Utilajul lui Flutur nu a putut ţine piept urgiei, dar, deh, dădea bine la ... etichetă. Nu am văzut ca în acest an agricultorul român să prospere sau măcar să o ducă un picuţ mai bine. Caravanele SAPARD, figuraţia cu \"la coasă\", cuponiada şi fotografiile cu Joiana nu-l încălzesc pe bietul român, copleşit de nevoi şi sătul de demagogie.\n" +
                        "Domnul Becsek-Garda Dezsö-Kálmán: La iniţiativa Ministerului Administraţiei şi Internelor şi a domnului prefect Constantin Strujan, în judeţul Harghita s-a organizat un control privind tăierile ilegale în pădurile judeţului. La această acţiune au participat Inspectoratul de Poliţie Harghita, Jandarmeria, Garda de Mediu, precum şi Direcţia Silvică Miercurea Ciuc, în total cu 835 controlori. Deşi au fost ocoliţi marii infractori, deşi mulţi dintre controlaţi au fost anunţaţi de controlori, prin telefoane mobile, despre verificări, cu toate acestea acţiunea \"Gaterul\" a dat rezultate. Au fost controlate 1200 gatere, în 292 de cazuri s-au dat amenzi în valoare de 196.247 lei noi. S-au constatat 79 de infracţiuni, dintre care în 51 de cazuri s-a pus în mişcare cercetarea şi urmărirea penală. Cu ocazia controlului s-au confiscat 522 meri cubi de masă lemnoasă, în valoare de 98.742 lei noi. La prima vedere acţiunea a fost reuşită. Însă controlorii aveau grijă să nu verifice pe principalii infractori. Mai mult chiar, la acţiune au participat şi angajaţii de la Direcţia Silvică Miercurea Ciuc, de la Compartimentul de Pază, personal silvic care ani de-a rândul a muşamalizat infracţiunile silvice şi a susţinut grupurile de interese implicate în exploatarea ilegală a masei lemnoase. Sunt curios ce soluţii se vor da în cazul celor 79 de infracţiuni din partea organelor de cercetae penală, în special din partea Parchetelor de pe lângă Judecătorii, precum şi din partea Parchetului de pe lângă Tribunalul judeţean Harghita, pentru că în ultimii 6 ani procurorii, în majoritatea cazurilor infracţionale, au dat soluţia de neîncepere a urmăririi penale, cu toate că prejudiciile aduse fondului forestier erau foarte importante. Tot aşa judecătorii, aproape în toate cazurile, au socotit că aceste infracţiuni nu reprezintă pericol social, contribuind astfel la muşamalizarea abuzurilor de serviciu a personalului silvic implicat în protejarea mafiei lemnului, precum şi persoanele care prin activittea lor au defrişat munţi întregi, contribuind astfel la alunecări de terenuri, contribuind astfel la o catastrofă ecologică. Din păcate, aceşti magistraţi au susţinere politică din partea Înaltei Curţi de Casaţie şi de către Consiliul Superior al Magistraturii, care se pare că este independent de Ministerul Justiţiei, dar este dependent de mafia lemnului. Stimată doamnă ministru Monica Macovei, Ar fi de preferat ca această independenţă a Consiliului Superior al Magistraturii să fie monitorizat de către o echipă specială din partea ministerului, pentru că soluţiile din ultima perioadă ale acestei instituţii care se autointitulează ca garant al independenţei justiţiei, ar susţine unele grupuri de interese, dar în special mafia lemnului. Vă mulţumesc pentru atenţie.\n"
        test(exampleString, 129)
    }

    @Test
    fun readReal2() {
        val exampleString =
                "Primăvara culturală a Fundației „Mitropolitul Bartolomeu“\n" +
                        "\n" +
                        "Fundația „Mitropolitul Bartolomeu“ dorește să marcheze spațiul cultural clujean al acestui început de primăvară prin două evenimente de referință, care vor avea loc în zilele de miercuri, 19 martie, și joi, 20 martie.\n" +
                        "Primul eveniment va fi Concertul Anual al Fundației „Mitropolitul Bartolomeu“, susținut de Cvartetul Transilvan, care va avea loc miercuri, 19 martie, de la orele 18:00, în Sala Auditorium Maximum a Universității „Babeș-Bolyai“ (Casa Universitarilor). Vor fi prezenți soliștii: Gabriel Croitoru - vioara I, Nicușor Silaghi - vioara a II-a, Marius Suărășan - violă și Vasile Jucan - violoncel, iar concertul va avea un program inedit și va cuprinde piese ale unor compozitori precum Joseph Haydn, Ludwig van Beethoven și Dimitri Șostakovici.\n" +
                        "La acest concert, violonistul Gabriel Croitoru va cânta pe vioara Guarnieri 1731 - Catedrala, care a aparținut compozitorului George Enescu.\n" +
                        "Concertul va fi prefațat muzical de un microrecital de muzică religioasă al Coralei „Psalmodia Transylvanica“, dirijată de pr. prof. univ. dr. Vasile Stanciu, decanul Facultății de Teologie Ortodoxă din Cluj-Napoca.\n" +
                        "Un element de mare valoare al acestei seri culturale va fi conferința susținută de scriitorul și criticul literar Dan C. Mihăilescu, prezent special la acest eveniment, la invitația Fundației „Mitropolitul Bartolomeu“. Dan C. Mihăilescu este critic literar, istoric literar și eseist român, renumitul realizator al emisiunii „Omul care aduce cartea“. Concertul anual al Fundației „Mitropolitul Bartolomeu“ a devenit tradiție pentru publicul clujean, aflându-se la a patra ediție, în acest an.\n" +
                        "Al doilea eveniment cultural, aflat sub semnul ineditului, îl reprezintă lansarea, joi, 19 martie, de la orele 17:00, în Amfiteatrul Noului Campus Teologic Ortodox „Nicolae Ivan“, din vecinătatea Consiliul Județean Cluj, a Filmului „Bartolomeu - Omul fără vicleșug“, realizat de MP 4 Studios.\n" +
                        "Filmul parcurge traseul vieții vrednicului de amintire mitropolit Bartolomeu Anania, punând în lumină atât personalitatea năvalnică, dar fermă, a omului Bisericii, cât și sensibilitatea păstorului de suflete și pe cea a scriitorului.\n" +
                        "Pornind din satul natal, itinerarul continuă pe cărările întortocheate, pline de culmi și abisuri, trece prin suferința închisorilor comuniste, prin marea experiență a libertății americane, pentru a se împlini în singurătatea rodnică de la Văratec și ajungând pe dealurile Niculei, în atelierul biblic și pe tronul arhieriei de la Cluj-Napoca. Personalitate marcantă și om de acțiune, deținut politic, om al culturii și al Duhului, neobosit propovăduitor al Cuvântului lui Dumnezeu, slujbașul Adevărului și al dreptății, dușman declarat al corupției de orice fel, vrednicul de pomenire mitropolit Bartolomeu rămâne un model al omului liber, conform celor precizate de consilierul cultural al Arhiepiscopiei Vadului, Feleacului și Clujului, părintele Bogdan Ivanov.\n" +
                        "\n"
        test(exampleString, 14)
    }

    @Test
    fun readReal3() {
        val exampleString =
                "Autoritățile județene vor dezbate astăzi o serie de detalii legate de plățile efectuate la legea venitului minim garantat pentru asistații ieșeni. Este vorba de aspecte ale \n" +
                        "Legii 416/2005 ce vor fi prezentate de către \n" +
                        "Agenția Județeană pentru Plăți și Inspecție \n" +
                        "Socială Iași, dar și unele autorități locale din județ. Ședința va avea loc cu începere de la ora 11:00, în Sala Galbenă a Prefecturii \n" +
                        "Iași și se va desfășura sub întrunirea Comisiei de Dialog Social. „Pe ordinea de zi vor figura mai multe puncte de discuții. \n" +
                        "Mai întâi se va dezbate Legea 416/2005 privind venitul minim garantat, detalii prezentate de reprezentanții Agenției Județene pentru Plăți și Inspecție Socială, de Direcția de Asistență Comunitară Iași și de primăriile din Lungani, Hârlău și Țibănești. În plus, se vor analiza și propuneri pentru modificarea proiectului Codului fiscal și proiectului de Cod de procedură fiscală. Aceste detalii vor fi prezentate de Consiliul Național al \n" +
                        "Întreprinderilor Private Mici și Mijlocii din \n" +
                        "România“, a spus Felix Guzgă, purtătorul de cuvânt al Prefecturii Iași. "
        test(exampleString, 7)
    }

    @Test
    fun readReal4() {
        val exampleString =
                "Ca să mor!... Să mă întunec pe vecie?... Prea e crud. Să nu mai gândesc nimica, nici să văd, nici să aud? Să nu mai primesc văpaia soarelui de primăvară, Ori să-mi răcoresc viața la un amurgit de sară, Și s-ascult, pe gânduri, doina trișca \n" +
                        "\n" +
                        "\n" +
                        "de la târlă, Sau duiosul plâns al apei șopotind noaptea pe gârlă? . . . . . . . . . . . . . . . . . . . . . . . A, e negrăit de lesne să-ți repezi un glonț în creier!... Dar pe cer scânteie luna, dar în iarbă cânt-un greier, \n" +
                        "\n" +
                        "\n" +
                        " Trișcă fluier. E-o mișcare, e un farmec care-n veci nu se mai curmă... Și când te întorci și cugeți, lung privind ce lași în urmă, Simți că nu-i chip să te saturi, c-a trăi i-o fericire. Primești orice suferință, dar eterna nesimțire, Nu. Durerea are-un capăt. Moartea-ți zice: Niciodată. Altă viață?...Altă lume?... i-o poveste minunată; Însă, ca să-i dea crezare, în veci mintea-mi n-o să poată. Eu o lacrimă de-aicea nu mi-aș da-o pentru toată. Nesfârșita fericire din viața de apoi. Câteva lopeți de țărnă... Rămâi țărnă și gunoi! Bine e să știi, la moarte, că o dungă lași un nume, Ca-i săpat la zidul nopții, c-ai muncit să-ți scoți în lume Din al creierului zbucium, ca pe-un diamant, ideea. "
        test(exampleString, 21)
    }

    @Test
    fun readReal5() {
        val exampleString =
                "Era Sâmbăta Paștelui. \n" +
                        "\n" +
                        " \n" +
                        "Sus pe deal, în satul depărtat ca la doi chilometri printre bălți, se auzeau clopotele bisericii... Și se aude așa de ciudat când ai friguri: aci foarte tare, aci aproape deloc... Noaptea care venea era noaptea Paștelui; scadența făgăduielii lui Gheorghe... \n" +
                        "\n" +
                        " \n" +
                        "\"Dar poate că l-au prins până acuma!\" \n" +
                        "\n" +
                        " \n" +
                        "...Oricum, Zibal mai stă la Podeni doar până la câștiu[2] viitor. Cu capitalul lui se poate deschide un negoț frumos în Ieși... În târg, Leiba să fie sănătos, o să șază aproape de comisie... O să cinstească pe comisar, pe ipistat, pe sergent... Cine plătește bine este bine păzit. \n" +
                        "\n" +
                        " \n" +
                        "Într-un târg așa mare, noaptea e zgomot și lumină, nu întuneric și tăcere ca în valea singuratică a Podenilor. E un han în Ieși, - acolo în colț, ce loc bun pentru o dughiană - un han unde toată noaptea cântă fetele la Café Chantan. Ce viață zgomotoasă și veselă! Acolo găsești la orice ceas, zi și noapte, pe domnul comisar cu fetele și cu alți poreți[3]. "
        test(exampleString, 14)
    }

    @Test
    fun readReal6() {
        val exampleString =
                "Responsabilitate și autoritate \n" +
                        " Responsabilitățile auditului intern în cadrul organizației trebuie să fie clar stabilite de către \n" +
                        "Conducere. Autoritatea sa trebuie să permită auditorului intern accesul deplin la documente, la bunuri și la \n" +
                        "persoanele care au un raport cu subiectul controlat.  \n" +
                        " Auditorul intern trebuie să fie liber să verifice, să estimeze valoarea politicilor, a planurilor, a \n" +
                        "procedurilor și a rapoartelor interne și externe.  \n" +
                        " Responsabilitățile auditului intern sunt:  \n" +
                        "- de a informa și consilia Conducerea, în acord cu deontologia Institutului Internațional al Auditorilor \n" +
                        "Interni; \n" +
                        "- de a coordona activitățile sale cu cele ale altor grupe de control, astfel încât să garanteze cât mai bine \n" +
                        "posibil securitatea controalelor și eficacitatea organizației.  \n" +
                        "În îndeplinirea funcțiilor sale, un auditor intern nu are asupra activităților pe care le controlează nici \n" +
                        "responsabilitate directă, nici autoritate. Din aceste motive, controlul și avizul auditorului intern nu trebuie în \n" +
                        "nici un mod să descarce alte persoane de exercitarea responsabilităților care le-au fost repartizate.  \n" +
                        " \n" +
                        "Independență \n" +
                        " Independența este esențială pentru eficacitatea auditului intern. Ea se obține, în primul rând, prin \n" +
                        "statutul său și prin obiectivitatea sa: \n" +
                        "- statutul funcției de audit intern și susținerea pe care i-o aduce Conducerea sunt determinanți majori ai \n" +
                        "acțiunii și ai valorii sale. Din acest motiv conducătorul funcției de audit intern, trebuie să fie atașat  unui \n" +
                        "compartiment a cărui autoritate acoperă un domeniu întins și asigură, la recomandările sale, o acțiune \n" +
                        "eficace, adecvată acțiunii sale eficace vis-à-vis de recomandările auditorului; \n" +
                        "- obiectivitatea este esențială funcției de audit. Pentru acest motiv, un auditor intern nu trebuie să \n" +
                        "definească și să pună în loc proceduri în orice activitate normală supusă la controlul său; aceasta ar fi de \n" +
                        "natură să compromită independența sa."
        test(exampleString, 14)
    }

    @Test
    fun readReal4FromFile() {
        FileReader("/Users/at/Documents/data/biblior-ro/txt/Povestiri.txt").use {
            BufferedReader(it).use {
                bufferedSentenceDetector.read(it).forEach { sentence ->
                    println("->$sentence<-")
                }
            }
        }

    }

    private fun test(exampleString: String, expectedSentenceCount: Int) {
        bufferedSentenceDetector.read(BufferedReader(exampleString.reader())).forEach { sentence ->
            println("->$sentence<-")
        }
        assertEquals(expectedSentenceCount, bufferedSentenceDetector.read(BufferedReader(exampleString.reader())).count())
    }


}
