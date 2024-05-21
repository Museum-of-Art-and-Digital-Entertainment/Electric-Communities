/* Parse tree structs */

YH_DEF_TAGGED(arrayAccess,
  YH_FLD_PTR(base,expression)
  YH_FLD_PTR(openBracketWS,string)
  YH_FLD_PTR(index,expression)
  YH_FLD_PTR(closeBracketWS,string)
  ,
  YH_ARG_FIELD(base)
  YH_ARG_FIELD(openBracketWS)
  YH_ARG_FIELD(index)
  YH_ARG_FIELD(closeBracketWS)
)

YH_DEF_TAGGED(arrayCreationExpression,
  YH_FLD_PTR(newWS,string)
  YH_FLD_PTR(baseType,type)
  YH_FLD_PTR(allocatedDimensions,dimensionList)
  YH_FLD_PTR(unallocatedDimensions,bracketsList)
  YH_FLD_PTR(initializer,arrayInitializer)
  ,
  YH_ARG_FIELD(newWS)
  YH_ARG_FIELD(baseType)
  YH_ARG_FIELD(allocatedDimensions)
  YH_ARG_FIELD(unallocatedDimensions)
  YH_ARG_FIELD(initializer)
)

YH_DEF_TAGGED(arrayInitializer,
  YH_FLD_PTR(openBraceWS,string)
  YH_FLD_PTR(initializers,variableInitializers)
  YH_FLD_PTR(commaWS,string)
  YH_FLD_PTR(closeBraceWS,string)
  ,
  YH_ARG_FIELD(openBraceWS)
  YH_ARG_FIELD(initializers)
  YH_ARG_FIELD(commaWS)
  YH_ARG_FIELD(closeBraceWS)
)

YH_DEF_TAGGED(arrayType,
  YH_FLD_PTR(baseType,type)
  YH_FLD_PTR(dimensions,bracketsList)
  ,
  YH_ARG_FIELD(baseType)
  YH_ARG_FIELD(dimensions)
)

YH_DEF_TAGGED(binop,
  YH_FLD_PTR(leftOpnd,expression)
  YH_FLD_PTR(operator,operator)
  YH_FLD_PTR(rightOpnd,expression)
  ,
  YH_ARG_FIELD(leftOpnd)
  YH_ARG_FIELD(operator)
  YH_ARG_FIELD(rightOpnd)
)

YH_DEF_TAGGED(block,
  YH_FLD_PTR(openBraceWS,string)
  YH_FLD_PTR(statements,statementList)
  YH_FLD_PTR(closeBraceWS,string)
  YH_FLD_PTR(distContext,distContext)
  ,
  YH_ARG_FIELD(openBraceWS)
  YH_ARG_FIELD(statements)
  YH_ARG_FIELD(closeBraceWS)
  YH_ARG_FIELD(distContext)
)

YH_DEF_TAGGED(brackets,
  YH_FLD_PTR(openBracketWS,string)
  YH_FLD_PTR(closeBracketWS,string)
  ,
  YH_ARG_FIELD(openBracketWS)
  YH_ARG_FIELD(closeBracketWS)
)
YH_LIST_DEF(brackets)

YH_DEF_TAGGED(breakStatement,
  YH_FLD_PTR(breakWS,string)
  YH_FLD_PTR(label,identifier)
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(breakWS)
  YH_ARG_FIELD(label)
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(caseLabel,
  YH_FLD_PTR(caseWS,string)
  YH_FLD_PTR(value,expression)
  YH_FLD_PTR(colonWS,string)
  ,
  YH_ARG_FIELD(caseWS)
  YH_ARG_FIELD(value)
  YH_ARG_FIELD(colonWS)
)

YH_DEF_TAGGED(castExpression,
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(type,type)
  YH_FLD_PTR(dimensions,bracketsList)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(opnd,expression)
  ,
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(dimensions)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(opnd)
)

YH_DEF_TAGGED(catch,
  YH_FLD_PTR(catchWS,string)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(param,formalParameter)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(body,block)
  ,
  YH_ARG_FIELD(catchWS)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(param)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(body)
)
YH_LIST_DEF(catch)

YH_DEF_TAGGED(characterLiteral,
  YH_FLD_PTR(characterWS,string)
  YH_FLD_PTR(character,string)
  ,
  YH_ARG_FIELD(characterWS)
  YH_ARG_FIELD(character)
)

YH_DEF_TAGGED(classDeclaration,
  YH_FLD_PTR(modifiers,modifierList)
  YH_FLD_PTR(classWS,string)
  YH_FLD_PTR(id,identifier)
  YH_FLD_PTR(extends,extends)
  YH_FLD_PTR(implements,implements)
  YH_FLD_PTR(body,classBody)
  ,
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(classWS)
  YH_ARG_FIELD(id)
  YH_ARG_FIELD(extends)
  YH_ARG_FIELD(implements)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(classBody,
  YH_FLD_PTR(openBraceWS,string)
  YH_FLD_PTR(fields,classBodyDeclarationList)
  YH_FLD_PTR(closeBraceWS,string)
  ,
  YH_ARG_FIELD(openBraceWS)
  YH_ARG_FIELD(fields)
  YH_ARG_FIELD(closeBraceWS)
)

YH_DEF_TAGGED(classBodyDeclaration,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)
YH_LIST_DEF(classBodyDeclaration)

YH_DEF_TAGGED(classSelection,
  YH_FLD_PTR(base,expression)
  YH_FLD_PTR(dotWS,string)
  YH_FLD_PTR(classWS,string)
  ,
  YH_ARG_FIELD(base)
  YH_ARG_FIELD(dotWS)
  YH_ARG_FIELD(classWS)
)

YH_DEF_TAGGED(compilationUnit,
  YH_FLD_PTR(package,packageDeclaration)
  YH_FLD_PTR(imports,importDeclarationList)
  YH_FLD_PTR(decls,typeDeclarationList)
  YH_FLD_PTR(finalWS,string)
  ,
  YH_ARG_FIELD(package)
  YH_ARG_FIELD(imports)
  YH_ARG_FIELD(decls)
  YH_ARG_FIELD(finalWS)
)
YH_LIST_DEF(compilationUnit)

YH_DEF_TAGGED(conditionalExpression,
  YH_FLD_PTR(test,expression)
  YH_FLD_PTR(qmarkWS,string)
  YH_FLD_PTR(trueValue,expression)
  YH_FLD_PTR(colonWS,string)
  YH_FLD_PTR(falseValue,expression)
  ,
  YH_ARG_FIELD(test)
  YH_ARG_FIELD(qmarkWS)
  YH_ARG_FIELD(trueValue)
  YH_ARG_FIELD(colonWS)
  YH_ARG_FIELD(falseValue)
)

YH_DEF_TAGGED(constructorDeclaration,
  YH_FLD_PTR(modifiers,modifierList)
  YH_FLD_PTR(declarator,methodDeclarator)
  YH_FLD_PTR(throws,throws)
  YH_FLD_PTR(body,constructorBody)
  ,
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(declarator)
  YH_ARG_FIELD(throws)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(constructorBody,
  YH_FLD_PTR(openBraceWS,string)
  YH_FLD_PTR(constructorInvocation,constructorInvocation)
  YH_FLD_PTR(statements,statementList)
  YH_FLD_PTR(closeBraceWS,string)
  ,
  YH_ARG_FIELD(openBraceWS)
  YH_ARG_FIELD(constructorInvocation)
  YH_ARG_FIELD(statements)
  YH_ARG_FIELD(closeBraceWS)
)

YH_DEF_TAGGED(constructorInvocation,
  YH_FLD_LONG(which)
  YH_FLD_PTR(whichWS,string)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(arguments,expressionSequence)
  YH_FLD_PTR(closeParenWS,string)
  ,
  YH_ARG_FIELD(which)
  YH_ARG_FIELD(whichWS)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(arguments)
  YH_ARG_FIELD(closeParenWS)
)

YH_DEF_TAGGED(continueStatement,
  YH_FLD_PTR(continueWS,string)
  YH_FLD_PTR(label,identifier)
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(continueWS)
  YH_ARG_FIELD(label)
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(defaultLabel,
  YH_FLD_PTR(defaultWS,string)
  YH_FLD_PTR(colonWS,string)
  ,
  YH_ARG_FIELD(defaultWS)
  YH_ARG_FIELD(colonWS)
)

YH_DEF_TAGGED(dimension,
  YH_FLD_PTR(openBracketWS,string)
  YH_FLD_PTR(size,expression)
  YH_FLD_PTR(closeBracketWS,string)
  ,
  YH_ARG_FIELD(openBracketWS)
  YH_ARG_FIELD(size)
  YH_ARG_FIELD(closeBracketWS)
)
YH_LIST_DEF(dimension)

YH_DEF_TAGGED(distop,
  YH_FLD_PTR(amperWS,string)
  YH_FLD_PTR(channel,name)
  ,
  YH_ARG_FIELD(amperWS)
  YH_ARG_FIELD(channel)
)

YH_DEF_TAGGED(doStatement,
  YH_FLD_PTR(doWS,string)
  YH_FLD_PTR(body,statement)
  YH_FLD_PTR(whileWS,string)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(condition,expression)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(doWS)
  YH_ARG_FIELD(body)
  YH_ARG_FIELD(whileWS)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(condition)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(ecatch,
  YH_FLD_PTR(catchWS,string)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(param,formalParameter)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(body,block)
  ,
  YH_ARG_FIELD(catchWS)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(param)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(body)
)
YH_LIST_DEF(ecatch)

YH_DEF_TAGGED(eclassDeclaration,
  YH_FLD_PTR(modifiers,modifierList)
  YH_FLD_PTR(eclassWS,string)
  YH_FLD_PTR(id,identifier)
  YH_FLD_PTR(extends,extends)
  YH_FLD_PTR(implements,implements)
  YH_FLD_PTR(body,classBody)
  ,
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(eclassWS)
  YH_ARG_FIELD(id)
  YH_ARG_FIELD(extends)
  YH_ARG_FIELD(implements)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(edebugStatement,
  YH_FLD_PTR(edebugWS,string)
  YH_FLD_PTR(body,block)
  ,
  YH_ARG_FIELD(edebugWS)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(eifStatement,
  YH_FLD_PTR(eifWS,string)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(condition,expression)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(thenPart,block)
  YH_FLD_PTR(eorifs,eorifStatementList)
  YH_FLD_PTR(elseWS,string)
  YH_FLD_PTR(elsePart,block)
  ,
  YH_ARG_FIELD(eifWS)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(condition)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(thenPart)
  YH_ARG_FIELD(eorifs)
  YH_ARG_FIELD(elseWS)
  YH_ARG_FIELD(elsePart)
)

YH_DEF_TAGGED(einterfaceDeclaration,
  YH_FLD_PTR(modifiers,modifierList)
  YH_FLD_PTR(einterfaceWS,string)
  YH_FLD_PTR(id,identifier)
  YH_FLD_PTR(extends,interfaceExtends)
  YH_FLD_PTR(body,classBody)
  ,
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(einterfaceWS)
  YH_ARG_FIELD(id)
  YH_ARG_FIELD(extends)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(ekeepStatement,
  YH_FLD_PTR(ekeepWS,string)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(expr,expression)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(body,statement)
  ,
  YH_ARG_FIELD(ekeepWS)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(expr)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(emethodDeclaration,
  YH_FLD_PTR(header,emethodHeader)
  YH_FLD_PTR(body,block)
  ,
  YH_ARG_FIELD(header)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(emethodHeader,
  YH_FLD_PTR(modifiers,modifierList)
  YH_FLD_PTR(emethodWS,string)
  YH_FLD_PTR(declarator,methodDeclarator)
  YH_FLD_PTR(throws,throws)
  ,
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(emethodWS)
  YH_ARG_FIELD(declarator)
  YH_ARG_FIELD(throws)
)

YH_DEF_TAGGED(emethodStub,
  YH_FLD_PTR(emethodWS,string)
  YH_FLD_PTR(header,emethodHeader)
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(emethodWS)
  YH_ARG_FIELD(header)
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(eorifStatement,
  YH_FLD_PTR(eorifWS,string)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(condition,expression)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(thenPart,block)
  ,
  YH_ARG_FIELD(eorifWS)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(condition)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(thenPart)
)
YH_LIST_DEF(eorifStatement)

YH_DEF_TAGGED(eorwhenStatement,
  YH_FLD_PTR(eorwhenWS,string)
  YH_FLD_PTR(target,expression)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(parameter,formalParameter)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(body,block)
  ,
  YH_ARG_FIELD(eorwhenWS)
  YH_ARG_FIELD(target)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(parameter)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(body)
)
YH_LIST_DEF(eorwhenStatement)

YH_DEF_TAGGED(ethrowStatement,
  YH_FLD_PTR(ethrowWS,string)
  YH_FLD_PTR(exception,expression)
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(ethrowWS)
  YH_ARG_FIELD(exception)
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(etryStatement,
  YH_FLD_PTR(etryWS,string)
  YH_FLD_PTR(body,block)
  YH_FLD_PTR(ecatches,ecatchList)
  ,
  YH_ARG_FIELD(etryWS)
  YH_ARG_FIELD(body)
  YH_ARG_FIELD(ecatches)
)

YH_DEF_TAGGED(ewheneverStatement,
  YH_FLD_PTR(ewheneverWS,string)
  YH_FLD_PTR(target,expression)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(parameter,formalParameter)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(body,block)
  ,
  YH_ARG_FIELD(ewheneverWS)
  YH_ARG_FIELD(target)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(parameter)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(ewhenStatement,
  YH_FLD_PTR(ewhenWS,string)
  YH_FLD_PTR(target,expression)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(parameter,formalParameter)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(body,block)
  YH_FLD_PTR(eorwhens,eorwhenStatementList)
  ,
  YH_ARG_FIELD(ewhenWS)
  YH_ARG_FIELD(target)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(parameter)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(body)
  YH_ARG_FIELD(eorwhens)
)

YH_DEF_TAGGED(expression,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)

YH_DEF_TAGGED(expressionSequence,
  YH_FLD_PTR(head,expressionSequence)
  YH_FLD_PTR(commaWS,string)
  YH_FLD_PTR(tail,expression)
,
  YH_ARG_FIELD(head)
  YH_ARG_FIELD(commaWS)
  YH_ARG_FIELD(tail)
)

YH_DEF_TAGGED(expressionStatement,
  YH_FLD_PTR(expression,expression)
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(expression)
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(extends,
  YH_FLD_PTR(extendsWS,string)
  YH_FLD_PTR(extendTypeName,name)
  ,
  YH_ARG_FIELD(extendsWS)
  YH_ARG_FIELD(extendTypeName)
)

YH_DEF_TAGGED(fieldDeclaration,
  YH_FLD_PTR(modifiers,modifierList)
  YH_FLD_PTR(type,type)
  YH_FLD_PTR(variables,variableDeclaratorList)
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(variables)
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(finally,
  YH_FLD_PTR(finallyWS,string)
  YH_FLD_PTR(body,block)
  ,
  YH_ARG_FIELD(finallyWS)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(formalParameter,
  YH_FLD_PTR(commaWS,string)
  YH_FLD_PTR(type,type)
  YH_FLD_PTR(declarator,variableDeclarator)
  ,
  YH_ARG_FIELD(commaWS)
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(declarator)
)
YH_LIST_DEF(formalParameter)

YH_DEF_TAGGED(forStatement,
  YH_FLD_PTR(forWS,string)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(init,expression)
  YH_FLD_PTR(semicolon1WS,string)
  YH_FLD_PTR(test,expression)
  YH_FLD_PTR(semicolon2WS,string)
  YH_FLD_PTR(incr,expression)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(body,statement)
  ,
  YH_ARG_FIELD(forWS)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(init)
  YH_ARG_FIELD(semicolon1WS)
  YH_ARG_FIELD(test)
  YH_ARG_FIELD(semicolon2WS)
  YH_ARG_FIELD(incr)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(identifier,
  YH_FLD_PTR(symbolWS,string)
  YH_FLD_PTR(symbol,symbol)
  ,
  YH_ARG_FIELD(symbolWS)
  YH_ARG_FIELD(symbol)
)

YH_DEF_TAGGED(ifStatement,
  YH_FLD_PTR(ifWS,string)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(condition,expression)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(thenPart,statement)
  YH_FLD_PTR(elseWS,string)
  YH_FLD_PTR(elsePart,statement)
  ,
  YH_ARG_FIELD(ifWS)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(condition)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(thenPart)
  YH_ARG_FIELD(elseWS)
  YH_ARG_FIELD(elsePart)
)

YH_DEF_TAGGED(implements,
  YH_FLD_PTR(implementsWS,string)
  YH_FLD_PTR(implementTypes,nameSequence)
  ,
  YH_ARG_FIELD(implementsWS)
  YH_ARG_FIELD(implementTypes)
)

YH_DEF_TAGGED(importDeclaration,
  YH_FLD_PTR(importWS,string)
  YH_FLD_PTR(importName,name)
  YH_FLD_PTR(dotWS,string)
  YH_FLD_PTR(starWS,string)
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(importWS)
  YH_ARG_FIELD(importName)
  YH_ARG_FIELD(dotWS)
  YH_ARG_FIELD(starWS)
  YH_ARG_FIELD(semicolonWS)
)
YH_LIST_DEF(importDeclaration)

YH_DEF_TAGGED(instanceCreationExpression,
  YH_FLD_PTR(newWS,string)
  YH_FLD_PTR(id,name)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(arguments,expressionSequence)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(immediateDef,classBody)
  ,
  YH_ARG_FIELD(newWS)
  YH_ARG_FIELD(id)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(arguments)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(immediateDef)
)

YH_DEF_TAGGED(interfaceDeclaration,
  YH_FLD_PTR(modifiers,modifierList)
  YH_FLD_PTR(interfaceWS,string)
  YH_FLD_PTR(id,identifier)
  YH_FLD_PTR(extends,interfaceExtends)
  YH_FLD_PTR(body,classBody)
  ,
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(interfaceWS)
  YH_ARG_FIELD(id)
  YH_ARG_FIELD(extends)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(interfaceExtends,
  YH_FLD_PTR(extendsWS,string)
  YH_FLD_PTR(extendTypes,nameSequence)
  ,
  YH_ARG_FIELD(extendsWS)
  YH_ARG_FIELD(extendTypes)
)

YH_DEF_TAGGED(keywordLiteral,
  YH_FLD_LONG(value)
  YH_FLD_PTR(valueWS,string)
  ,
  YH_ARG_FIELD(value)
  YH_ARG_FIELD(valueWS)
)

YH_DEF_TAGGED(labelledStatement,
  YH_FLD_PTR(label,identifier)
  YH_FLD_PTR(colonWS,string)
  YH_FLD_PTR(statement,statement)
  ,
  YH_ARG_FIELD(label)
  YH_ARG_FIELD(colonWS)
  YH_ARG_FIELD(statement)
)

YH_DEF_TAGGED(localVariableDeclaration,
  YH_FLD_PTR(type,type)
  YH_FLD_PTR(variables,variableDeclaratorList)
  ,
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(variables)
)

YH_DEF_TAGGED(methodDeclaration,
  YH_FLD_PTR(header,methodHeader)
  YH_FLD_PTR(body,block)
  ,
  YH_ARG_FIELD(header)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(methodDeclarator,
  YH_FLD_PTR(id,identifier)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(formalParameters,formalParameterList)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(brackets,bracketsList)
  ,
  YH_ARG_FIELD(id)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(formalParameters)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(brackets)
)

YH_DEF_TAGGED(methodHeader,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)

YH_DEF_TAGGED(methodHeaderTyped,
  YH_FLD_PTR(modifiers,modifierList)
  YH_FLD_PTR(type,type)
  YH_FLD_PTR(declarator,methodDeclarator)
  YH_FLD_PTR(throws,throws)
  ,
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(declarator)
  YH_ARG_FIELD(throws)
)

YH_DEF_TAGGED(methodHeaderVoid,
  YH_FLD_PTR(modifiers,modifierList)
  YH_FLD_PTR(voidWS,string)
  YH_FLD_PTR(declarator,methodDeclarator)
  YH_FLD_PTR(throws,throws)
  ,
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(voidWS)
  YH_ARG_FIELD(declarator)
  YH_ARG_FIELD(throws)
)

YH_DEF_TAGGED(methodInvocation,
  YH_FLD_PTR(method,expression)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(arguments,expressionSequence)
  YH_FLD_PTR(closeParenWS,string)
  ,
  YH_ARG_FIELD(method)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(arguments)
  YH_ARG_FIELD(closeParenWS)
)

YH_DEF_TAGGED(methodStub,
  YH_FLD_PTR(header,methodHeader)
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(header)
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(modifier,
  YH_FLD_LONG(modifier)
  YH_FLD_PTR(modifierWS,string)
  ,
  YH_ARG_FIELD(modifier)
  YH_ARG_FIELD(modifierWS)
)
YH_LIST_DEF(modifier)

YH_DEF_TAGGED(name,
  YH_FLD_PTR(prefix,name)
  YH_FLD_PTR(dotWS,string)
  YH_FLD_PTR(id,identifier)
  ,
  YH_ARG_FIELD(prefix)
  YH_ARG_FIELD(dotWS)
  YH_ARG_FIELD(id)
)
YH_LIST_DEF(name)

YH_DEF_TAGGED(nameSequence,
  YH_FLD_PTR(head,nameSequence)
  YH_FLD_PTR(commaWS,string)
  YH_FLD_PTR(tail,name)
  ,
  YH_ARG_FIELD(head)
  YH_ARG_FIELD(commaWS)
  YH_ARG_FIELD(tail)
)

YH_DEF_TAGGED(nullDeclaration,
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(nullStatement,
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(numberLiteral,
  YH_FLD_PTR(numberWS,string)
  YH_FLD_PTR(number,string)
  ,
  YH_ARG_FIELD(numberWS)
  YH_ARG_FIELD(number)
)

YH_DEF_TAGGED(operator,
  YH_FLD_LONG(op)
  YH_FLD_PTR(opWS,string)
  ,
  YH_ARG_FIELD(op)
  YH_ARG_FIELD(opWS)
)

YH_DEF_TAGGED(packageDeclaration,
  YH_FLD_PTR(packageWS,string)
  YH_FLD_PTR(packageName,name)
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(packageWS)
  YH_ARG_FIELD(packageName)
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(postop,
  YH_FLD_PTR(opnd,expression)
  YH_FLD_PTR(operator,operator)
  ,
  YH_ARG_FIELD(opnd)
  YH_ARG_FIELD(operator)
)

YH_DEF_TAGGED(primType,
  YH_FLD_LONG(type)
  YH_FLD_PTR(typeWS,string)
  ,
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(typeWS)
)

YH_DEF_TAGGED(returnStatement,
  YH_FLD_PTR(returnWS,string)
  YH_FLD_PTR(result,expression)
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(returnWS)
  YH_ARG_FIELD(result)
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(statement,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)
YH_LIST_DEF(statement)

YH_DEF_TAGGED(staticInitializer,
  YH_FLD_PTR(staticWS,string)
  YH_FLD_PTR(block,block)
  ,
  YH_ARG_FIELD(staticWS)
  YH_ARG_FIELD(block)
)

YH_DEF_TAGGED(stringLiteral,
  YH_FLD_PTR(stringWS,string)
  YH_FLD_PTR(string,string)
  ,
  YH_ARG_FIELD(stringWS)
  YH_ARG_FIELD(string)
)

YH_DEF_TAGGED(switchBlock,
  YH_FLD_PTR(openBraceWS,string)
  YH_FLD_PTR(switchGroups,switchGroupList)
  YH_FLD_PTR(closeBraceWS,string)
  ,
  YH_ARG_FIELD(openBraceWS)
  YH_ARG_FIELD(switchGroups)
  YH_ARG_FIELD(closeBraceWS)
)

YH_DEF_TAGGED(switchGroup,
  YH_FLD_PTR(labels,switchLabelList)
  YH_FLD_PTR(statements,statementList)
  ,
  YH_ARG_FIELD(labels)
  YH_ARG_FIELD(statements)
)
YH_LIST_DEF(switchGroup)

YH_DEF_TAGGED(switchLabel,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)
YH_LIST_DEF(switchLabel)

YH_DEF_TAGGED(switchStatement,
  YH_FLD_PTR(switchWS,string)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(condition,expression)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(body,switchBlock)
  ,
  YH_ARG_FIELD(switchWS)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(condition)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(subexpression,
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(value,expression)
  YH_FLD_PTR(closeParenWS,string)
  ,
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(value)
  YH_ARG_FIELD(closeParenWS)
)

YH_DEF_TAGGED(synchronizedStatement,
  YH_FLD_PTR(synchronizedWS,string)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(lock,expression)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(body,block)
  ,
  YH_ARG_FIELD(synchronizedWS)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(lock)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(body)
)

YH_DEF_TAGGED(throws,
  YH_FLD_PTR(throwsWS,string)
  YH_FLD_PTR(throwTypes,nameSequence)
  ,
  YH_ARG_FIELD(throwsWS)
  YH_ARG_FIELD(throwTypes)
)

YH_DEF_TAGGED(throwStatement,
  YH_FLD_PTR(throwWS,string)
  YH_FLD_PTR(exception,expression)
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(throwWS)
  YH_ARG_FIELD(exception)
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(tryStatement,
  YH_FLD_PTR(tryWS,string)
  YH_FLD_PTR(body,block)
  YH_FLD_PTR(catches,catchList)
  YH_FLD_PTR(finally,finally)
  ,
  YH_ARG_FIELD(tryWS)
  YH_ARG_FIELD(body)
  YH_ARG_FIELD(catches)
  YH_ARG_FIELD(finally)
)

YH_DEF_TAGGED(type,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)

YH_DEF_TAGGED(typeDeclaration,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)
YH_LIST_DEF(typeDeclaration)

YH_DEF_TAGGED(unop,
  YH_FLD_PTR(operator,operator)
  YH_FLD_PTR(opnd,expression)
  ,
  YH_ARG_FIELD(operator)
  YH_ARG_FIELD(opnd)
)

YH_DEF_TAGGED(variableDeclarationStatement,
  YH_FLD_PTR(declaration,localVariableDeclaration)
  YH_FLD_PTR(semicolonWS,string)
  ,
  YH_ARG_FIELD(declaration)
  YH_ARG_FIELD(semicolonWS)
)

YH_DEF_TAGGED(variableDeclarator,
  YH_FLD_PTR(commaWS,string)
  YH_FLD_PTR(id,identifier)
  YH_FLD_PTR(brackets,bracketsList)
  YH_FLD_PTR(equalsWS,string)
  YH_FLD_PTR(initializer,variableInitializer)
  ,
  YH_ARG_FIELD(commaWS)
  YH_ARG_FIELD(id)
  YH_ARG_FIELD(brackets)
  YH_ARG_FIELD(equalsWS)
  YH_ARG_FIELD(initializer)
)
YH_LIST_DEF(variableDeclarator)

YH_DEF_TAGGED(variableInitializer,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)

YH_DEF_TAGGED(variableInitializers,
  YH_FLD_PTR(head,variableInitializers)
  YH_FLD_PTR(commaWS,string)
  YH_FLD_PTR(tail,variableInitializer)
  ,
  YH_ARG_FIELD(head)
  YH_ARG_FIELD(commaWS)
  YH_ARG_FIELD(tail)
)

YH_DEF_TAGGED(whileStatement,
  YH_FLD_PTR(whileWS,string)
  YH_FLD_PTR(openParenWS,string)
  YH_FLD_PTR(condition,expression)
  YH_FLD_PTR(closeParenWS,string)
  YH_FLD_PTR(body,statement)
  ,
  YH_ARG_FIELD(whileWS)
  YH_ARG_FIELD(openParenWS)
  YH_ARG_FIELD(condition)
  YH_ARG_FIELD(closeParenWS)
  YH_ARG_FIELD(body)
)

/* Class file structs */

YH_DEF_TAGGED(constant_class_info,
  YH_FLD_WORD(name_index)
  ,
  YH_ARG_FIELD(name_index)
)

YH_DEF_TAGGED(constant_double,
  YH_FLD_LONG(high_bytes)
  YH_FLD_LONG(low_bytes)
  ,
  YH_ARG_FIELD(high_bytes)
  YH_ARG_FIELD(low_bytes)
)

YH_DEF_TAGGED(constant_fieldref_info,
  YH_FLD_WORD(class_index)
  YH_FLD_WORD(name_and_type_index)
  ,
  YH_ARG_FIELD(class_index)
  YH_ARG_FIELD(name_and_type_index)
)

YH_DEF_TAGGED(constant_float,
  YH_FLD_LONG(bytes)
  ,
  YH_ARG_FIELD(bytes)
)

YH_DEF_TAGGED(constant_integer,
  YH_FLD_LONG(bytes)
  ,
  YH_ARG_FIELD(bytes)
)

YH_DEF_TAGGED(constant_interfaceMethodref_info,
  YH_FLD_WORD(class_index)
  YH_FLD_WORD(name_and_type_index)
  ,
  YH_ARG_FIELD(class_index)
  YH_ARG_FIELD(name_and_type_index)
)

YH_DEF_TAGGED(constant_long,
  YH_FLD_LONG(high_bytes)
  YH_FLD_LONG(low_bytes)
  ,
  YH_ARG_FIELD(high_bytes)
  YH_ARG_FIELD(low_bytes)
)

YH_DEF_TAGGED(constant_methodref_info,
  YH_FLD_WORD(class_index)
  YH_FLD_WORD(name_and_type_index)
  ,
  YH_ARG_FIELD(class_index)
  YH_ARG_FIELD(name_and_type_index)
)

YH_DEF_TAGGED(constant_nameAndType_info,
  YH_FLD_WORD(name_index)
  YH_FLD_WORD(descriptor_index)
  ,
  YH_ARG_FIELD(name_index)
  YH_ARG_FIELD(descriptor_index)
)

YH_DEF_TAGGED(constant_string,
  YH_FLD_WORD(string_index)
  ,
  YH_ARG_FIELD(string_index)
)

YH_DEF_TAGGED(cp_info,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)

/* Other useful structs */

YH_DEF(class,
  YH_FLD_PTR(className,name)
  YH_FLD_BOOL(isEClass)
  YH_FLD_BOOL(isInterface)
  YH_FLD_BOOL(isImport)
  YH_FLD_PTR(declaration,typeDeclaration)
  YH_FLD_PTR(location,string)
  YH_FLD_PTR(methods,stringList)
  ,
  YH_ARG_FIELD(className)
  YH_ARG_FIELD(isEClass)
  YH_ARG_FIELD(isInterface)
  YH_ARG_FIELD(isImport)
  YH_ARG_FIELD(declaration)
  YH_ARG_FIELD(location)
  YH_ARG_FIELD(methods)
)

YH_DEF(classBucketEntry,
  YH_FLD_PTR(class,class)
  YH_FLD_PTR(nextFQN,classBucketEntry)
  YH_FLD_PTR(nextUnqual,classBucketEntry)
  ,
  YH_ARG_FIELD(class)
  YH_ARG_FIELD(nextFQN)
  YH_ARG_FIELD(nextUnqual)
)

YH_DEF(distContext,
  YH_FLD_PTR(names,nameList)
  YH_FLD_PTR(lowerContext,distContext)
  YH_FLD_PTR(nextContext,distContext)
  ,
  YH_ARG_FIELD(names)
  YH_ARG_FIELD(lowerContext)
  YH_ARG_FIELD(nextContext)
)

YH_LIST_DEF(string)

YH_DEF(stringStack,
  YH_FLD_PTR(stack,stringStack)
  YH_FLD_PTR(string,string)
  ,
  YH_ARG_FIELD(stack)
  YH_ARG_FIELD(string)
)

YH_DEF(unitInfo,
  YH_FLD_PTR(packageName,name)
  ,
  YH_ARG_FIELD(packageName)
)
YH_LIST_DEF(unitInfo)
