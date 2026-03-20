# Cloud Autoscaling Simulator

## Téma práce

Tématem této práce je vytvoření aplikace simulující chování cloudové služby zpracovávající příchozí požadavky. Moderní cloudové systémy musí reagovat na proměnlivou zátěž a dynamicky upravovat množství výpočetních zdrojů. Jedním z hlavních principů těchto systémů je automatické škálování infrastruktury a rozdělování zátěže mezi více instancí služby.

Cílem projektu je vytvořit interaktivní simulátor, který tyto principy modeluje a umožňuje sledovat jejich chování v reálném čase. Aplikace bude implementována v jazyce Java a bude obsahovat grafické uživatelské rozhraní vytvořené pomocí knihovny JavaFX.

## Manažerské shrnutí

Projekt představuje aplikaci simulující cloudovou službu zpracovávající příchozí požadavky. Požadavky jsou generovány podle zvoleného profilu zátěže a rozdělovány mezi jednotlivé instance služby. Během simulace jsou sledovány základní metriky systému, například latence, propustnost nebo délka fronty požadavků.

Součástí simulace je mechanismus automatického škálování, který může dynamicky upravovat počet instancí služby podle aktuálního zatížení. Pokud se zátěž zvýší, systém může přidat nové instance, při nízkém zatížení může některé instance odebrat.

Uživatel může simulaci spustit, zastavit nebo změnit její parametry a sledovat vývoj metrik pomocí grafů v grafickém rozhraní.

## Popis aplikace

Aplikace představuje cloudovou službu zpracovávající příchozí požadavky. Po spuštění simulace začnou do systému přicházet požadavky generované podle zvoleného profilu zátěže. Tyto požadavky jsou následně rozdělovány mezi jednotlivé instance služby pomocí load balanceru.

Každá instance služby zpracovává požadavky paralelně. Pokud fronta požadavků dosáhne maximální kapacity, mohou být nové požadavky odmítnuty. Během simulace jsou průběžně sbírány statistiky o chování systému.

Simulace sleduje například:

- throughput (requests/s)
- průměrnou latenci
- délku fronty požadavků
- počet aktivních instancí
- počet odmítnutých požadavků

Na základě těchto metrik může systém automaticky upravovat počet instancí služby.

## Funkcionalita aplikace

Aplikace umožňuje simulovat chování cloudové služby při různých typech zatížení.

Hlavní funkce aplikace:

- spuštění/zastavení simulace
- generování požadavků podle zvoleného profilu zátěže
- rozdělování požadavků mezi instance služby
- paralelní zpracování požadavků
- sledování metrik systému v reálném čase
- automatické škálování počtu instancí služby
- vizualizace metrik pomocí grafů

## Uživatelský scénář

Po spuštění aplikace se zobrazí hlavní okno obsahující ovládací prvky a grafy zobrazující stav simulovaného systému. Uživatel nejprve nastaví základní parametry simulace, například intenzitu příchozí zátěže nebo strategii rozdělování požadavků.

Po zahájení simulace začne systém generovat požadavky, které jsou postupně rozdělovány mezi jednotlivé instance služby a zpracovávány. Grafické rozhraní průběžně zobrazuje vývoj metrik systému, například latenci, propustnost a počet aktivních instancí.

## Uživatelské rozhraní

Aplikace obsahuje grafické rozhraní umožňující ovládání simulace a sledování jejího průběhu.

Hlavní prvky rozhraní:

- ovládací panel pro spuštění, pozastavení a restart simulace
- nastavení parametrů simulace
- grafy zobrazující vývoj metrik systému
- tabulka zobrazující jednotlivé instance služby

## Omezení simulace

Cílem aplikace není vytvořit plně realistický model cloudové infrastruktury, ale přehlednou simulaci základních principů fungování škálovatelných systémů.

Simulace proto obsahuje několik zjednodušení:

- požadavky nejsou přenášeny po skutečné síti
- systém nekomunikuje s reálnými servery
- infrastruktura není distribuována mezi více počítači
- čas je simulován v rámci běhu aplikace
