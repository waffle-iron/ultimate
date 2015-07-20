type ref;
type realVar;
type classConst;
// type Field x;
// var $HeapVar : <x>[ref, Field x]x;

const unique $null : ref ;
const unique $intArrNull : [int]int ;
const unique $realArrNull : [int]realVar ;
const unique $refArrNull : [int]ref ;

const unique $arrSizeIdx : int;
var $intArrSize : [int]int;
var $realArrSize : [realVar]int;
var $refArrSize : [ref]int;

var $stringSize : [ref]int;

//built-in axioms 
axiom ($arrSizeIdx == -1);

//note: new version doesn't put helpers in the perlude anymore//Prelude finished 



var java.lang.Object$ObjectList$value254 : Field ref;
var ObjectList$ObjectList$next255 : Field ref;
var int$Random$index0 : int;
var java.lang.String$lp$$rp$$Random$args256 : [int]ref;


// procedure is generated by joogie.
function {:inline true} $neref(x : ref, y : ref) returns (__ret : int) {
if (x != y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $realarrtoref($param00 : [int]realVar) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $modreal($param00 : realVar, $param11 : realVar) returns (__ret : realVar);



// procedure is generated by joogie.
function {:inline true} $leref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $modint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $gtref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $eqrealarray($param00 : [int]realVar, $param11 : [int]realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $addint(x : int, y : int) returns (__ret : int) {
(x + y)
}


// procedure is generated by joogie.
function {:inline true} $subref($param00 : ref, $param11 : ref) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $inttoreal($param00 : int) returns (__ret : realVar);



// procedure is generated by joogie.
function {:inline true} $shrint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $negReal($param00 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $ushrint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $refarrtoref($param00 : [int]ref) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $divref($param00 : ref, $param11 : ref) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $mulref($param00 : ref, $param11 : ref) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $neint(x : int, y : int) returns (__ret : int) {
if (x != y) then 1 else 0
}


	 //  @line: 4
// <ObjectList: void <init>(java.lang.Object,ObjectList)>
procedure void$ObjectList$$la$init$ra$$2231(__this : ref, $param_0 : ref, $param_1 : ref)
  modifies $HeapVar;
  requires ($neref((__this), ($null))==1);
 {
var r117 : ref;
var r218 : ref;
var r016 : ref;
Block34:
	r016 := __this;
	r117 := $param_0;
	r218 := $param_1;
	 assert ($neref((r016), ($null))==1);
	 //  @line: 5
	 call void$java.lang.Object$$la$init$ra$$28((r016));
	 assert ($neref((r016), ($null))==1);
	 //  @line: 6
	$HeapVar[r016, java.lang.Object$ObjectList$value254] := r117;
	 assert ($neref((r016), ($null))==1);
	 //  @line: 7
	$HeapVar[r016, ObjectList$ObjectList$next255] := r218;
	 return;
}


// procedure is generated by joogie.
function {:inline true} $ltreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $reftorefarr($param00 : ref) returns (__ret : [int]ref);



// procedure is generated by joogie.
function {:inline true} $gtint(x : int, y : int) returns (__ret : int) {
if (x > y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $reftoint($param00 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $addref($param00 : ref, $param11 : ref) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $xorreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $andref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $cmpreal(x : realVar, y : realVar) returns (__ret : int) {
if ($ltreal((x), (y)) == 1) then 1 else if ($eqreal((x), (y)) == 1) then 0 else -1
}


// procedure is generated by joogie.
function {:inline true} $addreal($param00 : realVar, $param11 : realVar) returns (__ret : realVar);



// procedure is generated by joogie.
function {:inline true} $gtreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $eqreal(x : realVar, y : realVar) returns (__ret : int) {
if (x == y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $ltint(x : int, y : int) returns (__ret : int) {
if (x < y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $newvariable($param00 : int) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $divint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $geint(x : int, y : int) returns (__ret : int) {
if (x >= y) then 1 else 0
}


	 //  @line: 2
// <Random: void <clinit>()>
procedure void$Random$$la$clinit$ra$$2235()
  modifies int$Random$index0;
 {
	 //  @line: 3
Block44:
	 //  @line: 3
	int$Random$index0 := 0;
	 return;
}


// procedure is generated by joogie.
function {:inline true} $mulint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $leint(x : int, y : int) returns (__ret : int) {
if (x <= y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $shlref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $eqrefarray($param00 : [int]ref, $param11 : [int]ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $reftointarr($param00 : ref) returns (__ret : [int]int);



// procedure is generated by joogie.
function {:inline true} $ltref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $mulreal($param00 : realVar, $param11 : realVar) returns (__ret : realVar);



// procedure is generated by joogie.
function {:inline true} $shrref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $ushrreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $shrreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $divreal($param00 : realVar, $param11 : realVar) returns (__ret : realVar);



// procedure is generated by joogie.
function {:inline true} $orint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $reftorealarr($param00 : ref) returns (__ret : [int]realVar);



// procedure is generated by joogie.
function {:inline true} $cmpref(x : ref, y : ref) returns (__ret : int) {
if ($ltref((x), (y)) == 1) then 1 else if ($eqref((x), (y)) == 1) then 0 else -1
}


	 //  @line: 11
// <ListDuplicate: void duplicate(ObjectList)>
procedure void$ListDuplicate$duplicate$2229($param_0 : ref)
  modifies $HeapVar;
 {
var r19 : ref;
var $z112 : int;
var $r26 : ref;
var r510 : ref;
var $r48 : ref;
var r02 : ref;
var z011 : int;
var $r37 : ref;
Block17:
	r02 := $param_0;
	 //  @line: 12
	r510 := r02;
	 //  @line: 13
	z011 := 1;
	 goto Block18;
	 //  @line: 14
Block18:
	 goto Block21, Block19;
	 //  @line: 14
Block21:
	 //  @line: 14
	 assume ($negInt(($eqref((r510), ($null))))==1);
	 goto Block22;
	 //  @line: 14
Block19:
	 assume ($eqref((r510), ($null))==1);
	 goto Block20;
	 //  @line: 16
Block22:
	 goto Block25, Block23;
	 //  @line: 24
Block20:
	 return;
	 //  @line: 16
Block25:
	 //  @line: 16
	 assume ($negInt(($eqint((z011), (0))))==1);
	 //  @line: 17
	$r26 := $newvariable((26));
	 assume ($neref(($newvariable((26))), ($null))==1);
	 assert ($neref((r510), ($null))==1);
	 //  @line: 17
	$r48 := $HeapVar[r510, java.lang.Object$ObjectList$value254];
	 assert ($neref((r510), ($null))==1);
	 //  @line: 17
	$r37 := $HeapVar[r510, ObjectList$ObjectList$next255];
	 assert ($neref(($r26), ($null))==1);
	 //  @line: 17
	 call void$ObjectList$$la$init$ra$$2231(($r26), ($r48), ($r37));
	 //  @line: 17
	r19 := $r26;
	 assert ($neref((r510), ($null))==1);
	 //  @line: 19
	$HeapVar[r510, ObjectList$ObjectList$next255] := r19;
	 goto Block24;
	 //  @line: 16
Block23:
	 assume ($eqint((z011), (0))==1);
	 goto Block24;
	 //  @line: 21
Block24:
	 assert ($neref((r510), ($null))==1);
	 //  @line: 21
	r510 := $HeapVar[r510, ObjectList$ObjectList$next255];
	 goto Block27;
	 //  @line: 22
Block27:
	 goto Block30, Block28;
	 //  @line: 22
Block30:
	 //  @line: 22
	 assume ($negInt(($neint((z011), (0))))==1);
	 //  @line: 14
	$z112 := 1;
	 goto Block31;
	 //  @line: 22
Block28:
	 assume ($neint((z011), (0))==1);
	 goto Block29;
	 //  @line: 22
Block31:
	 //  @line: 22
	z011 := $z112;
	 goto Block32;
	 //  @line: 14
Block29:
	 //  @line: 14
	$z112 := 0;
	 goto Block31;
	 //  @line: 22
Block32:
	 goto Block18;
}


// procedure is generated by joogie.
function {:inline true} $realtoint($param00 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $geref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $orreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $eqint(x : int, y : int) returns (__ret : int) {
if (x == y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $ushrref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $modref($param00 : ref, $param11 : ref) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $eqintarray($param00 : [int]int, $param11 : [int]int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $negRef($param00 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $lereal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $nereal(x : realVar, y : realVar) returns (__ret : int) {
if (x != y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $instanceof($param00 : ref, $param11 : classConst) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $xorref($param00 : ref, $param11 : ref) returns (__ret : int);



	 //  @line: 10
// <ObjectList: ObjectList createList()>
procedure ObjectList$ObjectList$createList$2232() returns (__ret : ref) {
var i025 : int;
var r224 : ref;
var $r022 : ref;
var $r123 : ref;
	 //  @line: 11
Block35:
	 //  @line: 11
	r224 := $null;
	 //  @line: 12
	 call i025 := int$Random$random$2234();
	 goto Block36;
	 //  @line: 13
Block36:
	 goto Block39, Block37;
	 //  @line: 13
Block39:
	 //  @line: 13
	 assume ($negInt(($leint((i025), (0))))==1);
	 //  @line: 14
	$r022 := $newvariable((40));
	 assume ($neref(($newvariable((40))), ($null))==1);
	 //  @line: 14
	$r123 := $newvariable((41));
	 assume ($neref(($newvariable((41))), ($null))==1);
	 assert ($neref(($r123), ($null))==1);
	 //  @line: 14
	 call void$java.lang.Object$$la$init$ra$$28(($r123));
	 assert ($neref(($r022), ($null))==1);
	 //  @line: 14
	 call void$ObjectList$$la$init$ra$$2231(($r022), ($r123), (r224));
	 //  @line: 14
	r224 := $r022;
	 //  @line: 15
	i025 := $addint((i025), (-1));
	 goto Block36;
	 //  @line: 13
Block37:
	 assume ($leint((i025), (0))==1);
	 goto Block38;
	 //  @line: 17
Block38:
	 //  @line: 17
	__ret := r224;
	 return;
}


// procedure is generated by joogie.
function {:inline true} $orref($param00 : ref, $param11 : ref) returns (__ret : int);



	 //  @line: 5
// <Random: int random()>
procedure int$Random$random$2234() returns (__ret : int)
  modifies int$Random$index0, $stringSize;
 {
var $i027 : int;
var $i332 : int;
var $i231 : int;
var $r128 : [int]ref;
var r029 : ref;
var $i130 : int;
	 //  @line: 6
Block43:
	 //  @line: 6
	$r128 := java.lang.String$lp$$rp$$Random$args256;
	 //  @line: 6
	$i027 := int$Random$index0;
	 assert ($geint(($i027), (0))==1);
	 assert ($ltint(($i027), ($refArrSize[$r128[$arrSizeIdx]]))==1);
	 //  @line: 6
	r029 := $r128[$i027];
	 //  @line: 7
	$i130 := int$Random$index0;
	 //  @line: 7
	$i231 := $addint(($i130), (1));
	 //  @line: 7
	int$Random$index0 := $i231;
	$i332 := $stringSize[r029];
	 //  @line: 8
	__ret := $i332;
	 return;
}


// procedure is generated by joogie.
function {:inline true} $intarrtoref($param00 : [int]int) returns (__ret : ref);



// <java.lang.Object: void <init>()>
procedure void$java.lang.Object$$la$init$ra$$28(__this : ref);



// procedure is generated by joogie.
function {:inline true} $subreal($param00 : realVar, $param11 : realVar) returns (__ret : realVar);



// procedure is generated by joogie.
function {:inline true} $shlreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $negInt(x : int) returns (__ret : int) {
if (x == 0) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $gereal($param00 : realVar, $param11 : realVar) returns (__ret : int);



	 //  @line: 5
// <ListDuplicate: void <init>()>
procedure void$ListDuplicate$$la$init$ra$$2228(__this : ref)  requires ($neref((__this), ($null))==1);
 {
var r01 : ref;
Block16:
	r01 := __this;
	 assert ($neref((r01), ($null))==1);
	 //  @line: 6
	 call void$java.lang.Object$$la$init$ra$$28((r01));
	 return;
}


// procedure is generated by joogie.
function {:inline true} $eqref(x : ref, y : ref) returns (__ret : int) {
if (x == y) then 1 else 0
}


// <Random: void <init>()>
procedure void$Random$$la$init$ra$$2233(__this : ref)  requires ($neref((__this), ($null))==1);
 {
var r026 : ref;
Block42:
	r026 := __this;
	 assert ($neref((r026), ($null))==1);
	 //  @line: 1
	 call void$java.lang.Object$$la$init$ra$$28((r026));
	 return;
}


// procedure is generated by joogie.
function {:inline true} $cmpint(x : int, y : int) returns (__ret : int) {
if (x < y) then 1 else if (x == y) then 0 else -1
}


// procedure is generated by joogie.
function {:inline true} $andint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $andreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// <java.lang.String: int length()>
procedure int$java.lang.String$length$59(__this : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $shlint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $xorint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $subint(x : int, y : int) returns (__ret : int) {
(x - y)
}


	 //  @line: 26
// <ListDuplicate: void main(java.lang.String[])>
procedure void$ListDuplicate$main$2230($param_0 : [int]ref)
  modifies java.lang.String$lp$$rp$$Random$args256, $stringSize;
 {
var r013 : [int]ref;
var r115 : ref;
Block33:
	r013 := $param_0;
	 //  @line: 27
	java.lang.String$lp$$rp$$Random$args256 := r013;
	 //  @line: 28
	 call r115 := ObjectList$ObjectList$createList$2232();
	 //  @line: 29
	 call void$ListDuplicate$duplicate$2229((r115));
	 return;
}

