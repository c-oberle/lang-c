[
	StdInclude                    -- H hs=1 [KW["#include"] H hs=0 [KW["<"] _1 KW[">"]]],
    Include                       -- H hs=1 [KW["#include"] H hs=0 [KW["\""] _1 KW["\""]]],
	CExtensionImport              -- H hs=1 [KW["#include"] H hs=0 [KW["\""] _1 KW["\""]]],
	Typedef                       -- KW["typedef"],
   	Auto                          -- KW["auto"],
   	Register                      -- KW["register"],
   	Static                        -- KW["static"],
   	Extern                        -- KW["extern"],
   	Inline                        -- KW["inline"],
   	StorageClassSpecifier         -- _1,
   	TypeSpecifier                 -- _1,
   	TypeQualifier                 -- _1,
   	FunSpecifier                  -- _1,
   	DecSpecifierSeq               -- _1,
   	DecSpecifierSeq.1:iter        -- _1,
   	If                            -- V vs=0 is=2 [H hs=0 [KW["if"] KW["("] _1 KW[")"]] _2],
   	If                            -- V vs=0 [V vs=0 is=2 [H hs=0 [KW["if"] KW["("] _1 KW[")"]] _2] V vs=0 is=2 [KW["else"] _3]],
   	Switch                        -- V [H hs=0 [KW["switch"] KW["("] _1 KW[")"]] _2],
   	Label                         -- H hs=0 [_1 KW[":"]] _2,
   	Case                          -- V vs=0 is=2 [H [KW["case"] H hs=0 [_1 KW[":"]]] _2],
   	Default                       -- V vs=0 is=2 [H hs=0 [KW["default"] KW[":"]] _1],
   	Break                         -- H hs=0 [KW["break"] KW[";"]],
   	Continue                      -- H hs=0 [KW["continue"] KW[";"]],
   	Return                        -- H [KW["return"] H hs=0 [_1 KW[";"]]],
   	Return.1:opt                  -- _1,
   	Goto                          -- KW["goto"] H hs=0 [_1 KW[";"]],
   	While                         -- V vs=0 [H hs=0 [KW["while"] KW["("] _1 KW[")"]] _2],
   	Do                            -- V vs=0 [KW["do"] _1 H hs=0 [KW["while"] KW["("] _2 KW[")"] KW[";"]]],
   	For                           -- V [H [H hs=0 [KW["for"] KW["("] _1 KW[";"]] H hs=0 [_2 KW[";"]] H hs=0 [_3 KW[")"]]] _4],
   	For.1:opt                     -- _1,
   	For.2:opt                     -- _1,
   	For.3:opt                     -- _1,
   	ForDec                        -- V vs=0 [H [H hs=0 [KW["for"] KW["("] _1] H hs=0 [_2 KW[";"]] H hs=0 [_3 KW[")"]]] _4],
    ForDec.2:opt                  -- _1,
   	ForDec.3:opt                  -- _1,
   	ExprStm                       -- H hs=0 [_1 KW[";"]],
   	ExprStm.1:opt                 -- _1,
   	LabeledStm                    -- _1,
   	SelectionStm                  -- _1,
   	IterationStm                  -- _1,
   	JumpStm                       -- _1,
   	Block                         -- V vs=0 [V vs=0 is=2 [KW["{"] _1] KW["}"]],
   	Block.1:opt                   -- V vs=0 [_1],
   	BlockItems                    -- _1,
   	BlockItems.1:iter             -- _1,
   	FunDef                        -- V vs=0 [H [_1 _2 _3] _4],
   	FunDef.3:opt                  -- _1,
   	DecSeq                        -- V vs=1 [_1],
	DecSeq.1:iter                 -- _1,
   	VarArgs                       -- H hs=0 [_1 KW[","]] KW["..."],
   	ParamList                     -- H [_1],
   	ParamList.1:iter-sep          -- H hs=0 [_1 KW[","]],
   	ParamDec                      -- H [_1 _2],
   	ParamDecAbstr                 -- H [_1 _2],
   	ParamDecAbstr.2:opt           -- _1,
   	Const                         -- KW["const"],
   	Restrict                      -- KW["restrict"],
   	Volatile                      -- KW["volatile"],
   	TypeQualifierList             -- _1,
   	TypeQualifierList.1:iter      -- _1,
   	TypeSpecifier                 -- _1,
   	TypeQualifier                 -- _1,
   	SpecifierQualifierList        -- _1,
   	SpecifierQualifierList.1:iter -- _1,
   	StructOrUnionDec              -- V vs=0 [V vs=0 is=2 [V vs=0 [H [_1 _2] KW["{"]] _3] KW["}"]],
   	StructOrUnionDec.2:opt        -- _1,
   	StructOrUnionSpecifier        -- H hs=1 [_1 _2],
   	Struct                        -- KW["struct"],
   	Union                         -- KW["union"],
   	StructDecList                 -- _1,
   	StructDecList.1:iter          -- _1,
   	StructDec                     -- H [_1 H hs=0 [_2 KW[";"]]],
   	StructDeclList                -- _1,
   	StructDeclList.1:iter-sep     -- H hs=0 [_1 KW[","]],
   	StructDeclarator              -- H hs=0 [_1 KW[":"] _2],
   	StructDeclarator.1:opt        -- _1,
   	Void                          -- KW["void"],
   	Char                          -- KW["char"],
   	Short                         -- KW["short"],
   	Int                           -- KW["int"],
   	Long                          -- KW["long"],
   	Float                         -- KW["float"],
   	Double                        -- KW["double"],
   	Signed                        -- KW["signed"],
   	Unsigned                      -- KW["unsigned"],
   	Bool                          -- KW["_Bool"],
   	Complex                       -- KW["_Complex"],
   	Imaginary                     -- KW["_Imaginary"],
   	StructOrUnion                 -- _1,
   	Enum                          -- _1,
   	TypedefName                   -- _1,
   	TypeName                      -- _1 _2,
   	TypeName.2:opt                -- _1,
   	AbstrDeclPointer              -- H hs=0 [_1 _2],
   	AbstrDeclPointer.1:opt        -- _1,
   	AbstrDeclPar                  -- H hs=0 [KW["("] _1 KW[")"]],
   	AbstrDeclAssign               -- H hs=0 [_1 KW["["] _2 KW["]"]],
   	AbstrDeclAssign.1:opt         -- _1,
   	AbstrDeclAssign.2:opt         -- _1,
   	AbstrDeclDeref                -- H hs=0 [_1 KW["["] KW["*"] KW["]"]],
   	AbstrDeclDeref.1:opt          -- _1,
   	AbstrDeclParamTypes           -- H hs=0 [_1 KW["("] _2 KW[")"]],
   	AbstrDeclParamTypes.1:opt     -- _1,
   	AbstrDeclParamTypes.2:opt     -- _1,
   	EnumSpecifier                 -- KW["enum"] _1 KW["{"] _2 KW["}"],
   	EnumSpecifier.1:opt           -- _1,
   	EnumSpecifierTrComma          -- KW["enum"] _1 KW["{"] _2 KW[","] KW["}"],
   	EnumSpecifierTrComma.1:opt    -- _1,
   	EnumIdentifier                -- KW["enum"] _1,
   	EnumeratorList                -- _1,
   	EnumeratorList.1:iter-sep     -- H hs=0 [_1 KW[","]],
   	EnumeratorConst               -- _1,
   	EnumeratorDef                 -- H hs=1 [_1 KW["="] _2],
   	IntLit                        -- _1,
   	CharLit                       -- _1,
   	FloatLit                      -- _1,
   	Lit                           -- _1,
   	StringLitSeq                  -- _1,
    StringLitSeq.1:iter           -- _1,
   	ParenExpr                     -- H hs=0 [KW["("] _1 KW[")"]],
   	Id                            -- _1,
   	Designation                   -- H [_1 KW["="]],
   	DesignatorList                -- _1,
    DesignatorList.1:iter         -- _1,
   	BracketDesignator             -- H hs=0 [KW["["] _1 KW["]"]],
   	DotDesignator                 -- H hs=0 [KW["."] _1],
   	Initializer                   -- H hs=0 [KW["{"] _1 KW["}"]],
    InitializerTrComma            -- H hs=0 [KW["{"] _1 KW[","] KW["}"]],
   	InitializerSeq                -- H [_1 _2],
   	InitializerSeq.1:opt          -- _1,
   	InitializerList               -- H [_1],
   	InitializerList.1:iter-sep    -- H hs=0 [_1 KW[","]],
   	PrimaryExpr                   -- _1,
   	ArraySubscript                -- H hs=0 [_1 KW["["] _2 KW["]"]],
   	Call                          -- H hs=0 [_1 KW["("] _2 KW[")"]],
   	Call.2:opt                    -- H [_1],
   	MemberAccess                  -- H hs=0 [_1 KW["."] _2],
   	PMemberAccess                 -- H hs=0 [_1 KW["->"] _2],
   	PostIncr                      -- H hs=0 [_1 KW["++"]],
   	PostDecr                      -- H hs=0 [_1 KW["--"]],
   	PostfixExpr                   -- H hs=0 [KW["("] _1 KW[")"]] KW["{"] _2 KW["}"],
   	PostfixExprTrComma            -- H hs=0 [KW["("] _1 KW[")"]] KW["{"] _2 KW[","] KW["}"],
   	PreIncr                       -- H hs=0 [KW["++"] _1],
   	PreDecr                       -- H hs=0 [KW["--"] _1],
   	CastExpr                      -- H hs=0 [_1 _2],
   	SizeOf                        -- KW["sizeof"] _1,
   	SizeOfType                    -- H hs=0 [KW["sizeof"] KW["("] _1 KW[")"]],
   	Deref                         -- KW["*"],
   	Ref                           -- KW["&"],
   	Plus                          -- KW["+"],
   	Minus                         -- KW["-"],
   	Not                           -- KW["!"],
   	Complement                    -- KW["~"],
   	Cast                          -- H hs=0 [KW["("] _1 KW[")"]] _2,
   	Mul                           -- H [_1 KW["*"] _2],
    Div                           -- H [_1 KW["/"] _2],
    Mod                           -- H [_1 KW["%"] _2],
    Plus                          -- H [_1 KW["+"] _2],
    Minus                         -- H [_1 KW["-"] _2],
    LeftShift                     -- H [_1 KW["<<"] _2],
    RightShift                    -- H [_1 KW[">>"] _2],
    Lt                            -- H [_1 KW["<"] _2],
    Gt                            -- H [_1 KW[">"] _2],
    LtEq                          -- H [_1 KW["<="] _2],
    GtEq                          -- H [_1 KW[">="] _2],
    Eq                            -- H [_1 KW["=="] _2],
    NotEq                         -- H [_1 KW["!="] _2],
    And                           -- H [_1 KW["&"] _2],
    ExcOr                         -- H [_1 KW["^"] _2],
    Or                            -- H [_1 KW["|"] _2],
    LogicalAnd                    -- H [_1 KW["&&"] _2],
    LogicalOr                     -- H [_1 KW["||"] _2],
    Expr                          -- _1,
   	Expr.1:iter-sep               -- H hs=0 [_1 KW[","]],
   	Assign                        -- H [_1 _2 _3],
   	AssignOp                      -- KW["="],
   	AssignOpMul                   -- KW["*="],
   	AssignOpDiv                   -- KW["/="],
   	AssignOpMod                   -- KW["%="],
   	AssignOpPlus                  -- KW["+="],
   	AssignOpMinus                 -- KW["-="],
   	AssignOpShiftR                -- KW[">>="],
   	AssignOpShiftL                -- KW["<<="],
   	AssignOpAnd                   -- KW["&="],
   	AssignOpXor                   -- KW["^="],
   	AssignOpOr                    -- KW["|="],
   	Cond                          -- _1 KW["?"] _2 KW[":"] _3,
   	ConstExpr                     -- _1,
   	Declarator                    -- H hs=0 [_1 _2],
   	Declarator.1:opt              -- _1,
   	DeclPar                       -- H hs=0 [KW["("] _1 KW[")"]],
   	DeclQualifierAssign           -- H hs=0 [_1 KW["["] _2] H hs=0 [_3 KW["]"]],
   	DeclQualifierAssign.2:opt     -- _1,
   	DeclQualifierAssign.3:opt     -- _1,
   	DeclStaticAssign              -- H hs=0 [_1 KW["["] KW["static"]] _2 H hs=0 [_3 KW["]"]],
   	DeclStaticAssign.2:opt        -- _1,
   	DeclQualifierStaticAssign     -- H hs=0 [_1 KW["["] _2] KW["static"] H hs=0 [_3 KW["]"]],
   	DeclTypeQualifiers            -- H hs=0 [_1 KW["["] _2 KW["*"] KW["]"]],
   	DeclTypeQualifiers.2:opt      -- _1,
   	DeclParams                    -- H hs=0 [_1 KW["("] _2 KW[")"]],
   	DeclIds                       -- H hs=0 [_1 KW["("] _2 KW[")"]],
   	DeclIds.2:opt                 -- H [_1],
   	PointerSeq                    -- _1,
   	PointerSeq.1:iter             -- _1,
   	Pointer                       -- H hs=0 [KW["*"] _1],
   	Pointer.1:opt                 -- _1,
   	IdentifierList                -- _1,
   	IdentifierList.1:iter-sep     -- H hs=0 [_1 KW[","]],
   	Dec                           -- H [_1 H hs=0 [_2 KW[";"]]],
   	Dec.2:opt                     -- _1,
   	InitDeclaratorList            -- H [_1],
   	InitDeclaratorList.1:iter-sep -- H hs=0 [_1 KW[","]],
   	InitDeclarator                -- H [_1 KW["="] _2],
	TranslationUnit               -- _1,
   	TranslationUnit.1:opt         -- _1,
   	ExtDecSeq                     -- V vs=1 [_1],
   	ExtDecSeq.1:iter              -- _1,
   	ExtDec                        -- _1
]