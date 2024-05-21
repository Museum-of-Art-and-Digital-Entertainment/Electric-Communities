YH_DEF_TAGGED(anyBinding,
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_LONG(bindingType)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(bindingType)
)

YH_DEF_TAGGED(arrayDeclarator,
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(dimensions,arraySizeList)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(dimensions)
)

YH_DEF_TAGGED(arraySize,
  YH_FLD_LONG(dimension)
  ,
  YH_ARG_FIELD(dimension)
)
YH_LIST_DEF(arraySize)

YH_DEF_TAGGED(attribute,
  YH_FLD_PTR(type,attributeType)
  YH_FLD_PTR(value,expr)
  YH_FLD_PTR(worth,typedValue)
  ,
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(value)
  YH_ARG_FIELD(worth)
)
YH_LIST_DEF(attribute)

YH_DEF_TAGGED(attributeDef,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_PTR(type,typeSpec)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(type)
)

YH_DEF_TAGGED(attributeRef,
  YH_FLD_PTR(name,symbolRef)
  YH_FLD_PTR(expr,expr)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(expr)
)

YH_DEF_TAGGED(attributeType,
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_LONG(bindingType)
  YH_FLD_PTR(type,type)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(bindingType)
  YH_ARG_FIELD(type)
)

YH_DEF_TAGGED(binding,
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_PTR(value,anyBinding)
  YH_FLD_BOOL(isExport)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(value)
  YH_ARG_FIELD(isExport)
)
YH_LIST_DEF(binding)

YH_DEF_TAGGED(binop,
  YH_FLD_PTR(left,expr)
  YH_FLD_LONG(op)
  YH_FLD_PTR(right,expr)
  ,
  YH_ARG_FIELD(left)
  YH_ARG_FIELD(op)
  YH_ARG_FIELD(right)
)

YH_DEF_TAGGED(boolLit,
  YH_FLD_BOOL(value)
  ,
  YH_ARG_FIELD(value)
)

YH_DEF_TAGGED(caseLabel,
  YH_FLD_LONG(aCase)
  ,
  YH_ARG_FIELD(aCase)
)
YH_LIST_DEF(caseLabel)

YH_DEF_TAGGED(caseLabelDecl,
  YH_FLD_PTR(aCase,expr)
  ,
  YH_ARG_FIELD(aCase)
)
YH_LIST_DEF(caseLabelDecl)

YH_DEF_TAGGED(charLit,
  YH_FLD_LONG(value)
  ,
  YH_ARG_FIELD(value)
)

YH_DEF_TAGGED(codeAtt,
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_LONG(bindingType)
  YH_FLD_PTR(modifiers,codeModifierList)
  YH_FLD_LONG(type)
  YH_FLD_PTR(inherits,codeInheritList)
  YH_FLD_PTR(methodCode,string)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(bindingType)
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(inherits)
  YH_ARG_FIELD(methodCode)
)
YH_LIST_DEF(codeAtt)

YH_DEF_TAGGED(codeDef,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_PTR(modifiers,codeModifierList)
  YH_FLD_LONG(type)
  YH_FLD_PTR(inherits,codeInheritList)
  YH_FLD_PTR(methodCode,string)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(inherits)
  YH_ARG_FIELD(methodCode)
)
YH_LIST_DEF(codeDef)

YH_DEF_TAGGED(codeInherit,
  YH_FLD_LONG(type)
  YH_FLD_PTR(parents,pluribusTypeList)
  ,
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(parents)
)
YH_LIST_DEF(codeInherit)

YH_DEF_TAGGED(codeModifier,
  YH_FLD_LONG(type)
  ,
  YH_ARG_FIELD(type)
)
YH_LIST_DEF(codeModifier)

YH_DEF_TAGGED(condop,
  YH_FLD_PTR(cond,expr)
  YH_FLD_PTR(thenPart,expr)
  YH_FLD_PTR(elsePart,expr)
  ,
  YH_ARG_FIELD(cond)
  YH_ARG_FIELD(thenPart)
  YH_ARG_FIELD(elsePart)
)

YH_DEF_TAGGED(data,
  YH_FLD_PTR(data,string)
  ,
  YH_ARG_FIELD(data)
)
YH_LIST_DEF(data)

YH_DEF_TAGGED(dataAtt,
  YH_FLD_PTR(data,string)
  ,
  YH_ARG_FIELD(data)
)

YH_DEF_TAGGED(declarator,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)
YH_LIST_DEF(declarator)

YH_DEF_TAGGED(defType,
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_LONG(bindingType)
  YH_FLD_PTR(dimensions,arraySizeList)
  YH_FLD_PTR(type,type)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(bindingType)
  YH_ARG_FIELD(dimensions)
  YH_ARG_FIELD(type)
)

YH_DEF_TAGGED(deliverAtt,
  YH_FLD_LONG(scope)
  YH_FLD_PTR(source,symbol)
  YH_FLD_PTR(target,symbol)
  YH_FLD_PTR(contextScope,scope)
  ,
  YH_ARG_FIELD(scope)
  YH_ARG_FIELD(source)
  YH_ARG_FIELD(target)
  YH_ARG_FIELD(contextScope)
)
YH_LIST_DEF(deliverAtt)

YH_DEF_TAGGED(elem,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)
YH_LIST_DEF(elem)

YH_DEF_TAGGED(elementSpec,
  YH_FLD_PTR(type,typeSpec)
  YH_FLD_PTR(declarator,declarator)
  ,
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(declarator)
)

YH_DEF_TAGGED(emethodAtt,
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(params,parameterDeclList)
  YH_FLD_PTR(throws,scopedRefList)
  YH_FLD_PTR(methodCode,string)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(params)
  YH_ARG_FIELD(throws)
  YH_ARG_FIELD(methodCode)
)

YH_DEF_TAGGED(enumType,
  YH_FLD_PTR(enumerators,symbolList)
  ,
  YH_ARG_FIELD(enumerators)
)

YH_DEF_TAGGED(enumTypeDecl,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(enumerators,symbolList)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(enumerators)
)

YH_DEF_TAGGED(expr,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)
YH_LIST_DEF(expr)

YH_DEF_TAGGED(function,
  YH_FLD_LONG(modifiers)
  YH_FLD_PTR(resultType,type)
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(params,parameterDeclList)
  YH_FLD_PTR(throws,scopedRefList)
  YH_FLD_PTR(methodCode,string)
  ,
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(resultType)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(params)
  YH_ARG_FIELD(throws)
  YH_ARG_FIELD(methodCode)
)
YH_LIST_DEF(function)

YH_DEF_TAGGED(functionAtt,
  YH_FLD_LONG(modifiers)
  YH_FLD_PTR(resultType,typeSpec)
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(params,parameterDeclList)
  YH_FLD_PTR(throws,scopedRefList)
  YH_FLD_PTR(methodCode,string)
  ,
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(resultType)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(params)
  YH_ARG_FIELD(throws)
  YH_ARG_FIELD(methodCode)
)

YH_DEF_TAGGED(genericDef,
  YH_FLD_PTR(info,info)
  ,
  YH_ARG_FIELD(info)
)

YH_DEF_TAGGED(genericRef,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)

YH_DEF_TAGGED(implementsAtt,
  YH_FLD_PTR(name,symbolRef)
  ,
  YH_ARG_FIELD(name)
)
YH_LIST_DEF(implementsAtt)

YH_DEF_TAGGED(importAtt,
  YH_FLD_PTR(name,symbolRef)
  YH_FLD_BOOL(isPackageImport)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(isPackageImport)
)
YH_LIST_DEF(importAtt)

YH_DEF_TAGGED(importBinding,
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_LONG(bindingType)
  YH_FLD_PTR(filePath,string)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(bindingType)
  YH_ARG_FIELD(filePath)
)

YH_DEF_TAGGED(info,
  YH_FLD_LONG(modifiers)
  YH_FLD_LONG(lineNumber)
  YH_FLD_LONG(linePos)
  ,
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(lineNumber)
  YH_ARG_FIELD(linePos)
)

YH_DEF_TAGGED(ingredient,
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(kind,kind)
  YH_FLD_PTR(deliverAtts,deliverAttList)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(kind)
  YH_ARG_FIELD(deliverAtts)
)
YH_LIST_DEF(ingredient)

YH_DEF_TAGGED(ingredientAtt,
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(elems,elemList)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(elems)
)

YH_DEF_TAGGED(ingredientImpl,
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_LONG(bindingType)
  YH_FLD_PTR(attributes,attributeList)
  YH_FLD_PTR(kind,kind)
  YH_FLD_PTR(neighbors,neighborList)
  YH_FLD_PTR(stateBundle,stateBundle)
  YH_FLD_PTR(vars,variableList)
  YH_FLD_PTR(functions,functionList)
  YH_FLD_PTR(initBlocks,methodList)
  YH_FLD_PTR(methods,methodList)
  YH_FLD_PTR(data,dataList)
  YH_FLD_PTR(implements,implementsAttList)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(bindingType)
  YH_ARG_FIELD(attributes)
  YH_ARG_FIELD(kind)
  YH_ARG_FIELD(neighbors)
  YH_ARG_FIELD(stateBundle)
  YH_ARG_FIELD(vars)
  YH_ARG_FIELD(functions)
  YH_ARG_FIELD(initBlocks)
  YH_ARG_FIELD(methods)
  YH_ARG_FIELD(data)
  YH_ARG_FIELD(implements)
)

YH_DEF_TAGGED(ingredientImplDef,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_PTR(elems,elemList)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(elems)
)

YH_DEF_TAGGED(ingredientImplRef,
  YH_FLD_PTR(name,symbolRef)
  ,
  YH_ARG_FIELD(name)
)

YH_DEF_TAGGED(ingredientRole,
  YH_FLD_PTR(ingredients,symbolList)
  YH_FLD_PTR(template,template)
  ,
  YH_ARG_FIELD(ingredients)
  YH_ARG_FIELD(template)
)
YH_LIST_DEF(ingredientRole)

YH_DEF_TAGGED(initBlockAtt,
  YH_FLD_LONG(initType)
  YH_FLD_PTR(params,parameterDeclList)
  YH_FLD_PTR(throws,scopedRefList)
  YH_FLD_PTR(methodCode,string)
  ,
  YH_ARG_FIELD(initType)
  YH_ARG_FIELD(params)
  YH_ARG_FIELD(throws)
  YH_ARG_FIELD(methodCode)
)

YH_DEF_TAGGED(kindDef,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_PTR(elems,elemList)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(elems)
)

YH_DEF_TAGGED(kindRef,
  YH_FLD_PTR(name,symbolRef)
  ,
  YH_ARG_FIELD(name)
)

YH_DEF_TAGGED(kind,
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_LONG(bindingType)
  YH_FLD_PTR(attributes,attributeList)
  YH_FLD_PTR(protos,protoDefList)
  YH_FLD_PTR(implements,implementsAttList)
  YH_FLD_PTR(kinds,kindList)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(bindingType)
  YH_ARG_FIELD(attributes)
  YH_ARG_FIELD(protos)
  YH_ARG_FIELD(implements)
  YH_ARG_FIELD(kinds)
)
YH_LIST_DEF(kind)

YH_DEF_TAGGED(makeAtt,
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(inits,symbolList)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(inits)
)

YH_DEF_TAGGED(mapAtt,
  YH_FLD_LONG(scope)
  YH_FLD_PTR(mapFrom,symbol)
  YH_FLD_PTR(mapTo,symbol)
  ,
  YH_ARG_FIELD(scope)
  YH_ARG_FIELD(mapFrom)
  YH_ARG_FIELD(mapTo)
)
YH_LIST_DEF(mapAtt)

YH_DEF_TAGGED(member,
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(dimensions,arraySizeList)
  YH_FLD_PTR(type,type)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(dimensions)
  YH_ARG_FIELD(type)
)
YH_LIST_DEF(member)

YH_DEF_TAGGED(memberDecl,
  YH_FLD_PTR(type,typeSpec)
  YH_FLD_PTR(declarators,declaratorList)
  ,
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(declarators)
)
YH_LIST_DEF(memberDecl)

YH_DEF_TAGGED(method,
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(params,parameterDeclList)
  YH_FLD_PTR(throws,scopedRefList)
  YH_FLD_PTR(methodCode,string)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(params)
  YH_ARG_FIELD(throws)
  YH_ARG_FIELD(methodCode)
)
YH_LIST_DEF(method)

YH_DEF_TAGGED(nameSpace,
  YH_FLD_ENUM(bindingType,
    YH_FLD_ENUM_CASE(BIND_TYPE)
    YH_FLD_ENUM_CASE(BIND_ATTRIBUTE)
    YH_FLD_ENUM_CASE(BIND_CLASS)
    YH_FLD_ENUM_CASE(BIND_INTERFACE)
    YH_FLD_ENUM_CASE(BIND_ECLASS)
    YH_FLD_ENUM_CASE(BIND_EINTERFACE)
    YH_FLD_ENUM_CASE(BIND_KIND)
    YH_FLD_ENUM_CASE(BIND_PRESENCE_STRUCTURE)
    YH_FLD_ENUM_CASE(BIND_UNUM_STRUCTURE)
    YH_FLD_ENUM_CASE(BIND_INGREDIENT_IMPL)
    YH_FLD_ENUM_CASE(BIND_PRESENCE_IMPL)
    YH_FLD_ENUM_CASE(BIND_UNUM_IMPL)
    YH_FLD_ENUM_CASE(BIND_UNIT)
    YH_FLD_ENUM_CASE(BIND_LIMIT)
  )
  YH_FLD_PTR(bindings,bindingList)
  ,
  YH_ARG_FIELD(bindingType)
  YH_ARG_FIELD(bindings)
)
YH_LIST_DEF(nameSpace)

YH_DEF_TAGGED(neighbor,
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(kind,kind)
  YH_FLD_BOOL(isPlural)
  YH_FLD_BOOL(isPresence)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(kind)
  YH_ARG_FIELD(isPlural)
  YH_ARG_FIELD(isPresence)
)
YH_LIST_DEF(neighbor)

YH_DEF_TAGGED(neighborAtt,
  YH_FLD_PTR(name,symbol)
  YH_FLD_BOOL(isPlural)
  YH_FLD_BOOL(isPresence)
  YH_FLD_PTR(kind,genericRef)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(isPlural)
  YH_ARG_FIELD(isPresence)
  YH_ARG_FIELD(kind)
)

YH_DEF_TAGGED(nestedElem,
  YH_FLD_PTR(elems,elemList)
  ,
  YH_ARG_FIELD(elems)
)

YH_DEF_TAGGED(numLit,
  YH_FLD_LONG(value)
  ,
  YH_ARG_FIELD(value)
)

YH_DEF_TAGGED(outerRef,
  YH_FLD_LONG(level)
  YH_FLD_PTR(ref,symbolRef)
  ,
  YH_ARG_FIELD(level)
  YH_ARG_FIELD(ref)
)

YH_DEF_TAGGED(packageAtt,
  YH_FLD_PTR(name,symbolRef)
  ,
  YH_ARG_FIELD(name)
)

YH_DEF_TAGGED(parameterDecl,
  YH_FLD_PTR(type,typeSpec)
  YH_FLD_PTR(name,symbol)
  YH_FLD_LONG(dimensions)
  ,
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(dimensions)
)
YH_LIST_DEF(parameterDecl)

YH_DEF_TAGGED(pluribusType,
  YH_FLD_PTR(type,symbolRef)
  YH_FLD_LONG(mangle)
  ,
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(mangle)
)
YH_LIST_DEF(pluribusType)

YH_DEF_TAGGED(presence,
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(makes,symbol)
  YH_FLD_PTR(conditionals,presenceCondList)
  YH_FLD_PTR(kind,kind)
  YH_FLD_BOOL(isPrime)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(makes)
  YH_ARG_FIELD(conditionals)
  YH_ARG_FIELD(kind)
  YH_ARG_FIELD(isPrime)
)
YH_LIST_DEF(presence)

YH_DEF_TAGGED(presenceAtt,
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(makes,symbol)
  YH_FLD_PTR(conditionals,presenceCondList)
  YH_FLD_PTR(presence,kindRef)
  YH_FLD_BOOL(isPrime)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(makes)
  YH_ARG_FIELD(conditionals)
  YH_ARG_FIELD(presence)
  YH_ARG_FIELD(isPrime)
)

YH_DEF_TAGGED(presenceBehavior,
  YH_FLD_PTR(behaviors, symbolList)
  ,
  YH_ARG_FIELD(behaviors)
)

YH_DEF_TAGGED(presenceCond,
  YH_FLD_PTR(expr,expr)
  YH_FLD_PTR(makes,symbol)
  ,
  YH_ARG_FIELD(expr)
  YH_ARG_FIELD(makes)
)
YH_LIST_DEF(presenceCond)

YH_DEF_TAGGED(presenceImpl,
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_LONG(bindingType)
  YH_FLD_PTR(attributes,attributeList)
  YH_FLD_PTR(structure,presenceStructure)
  YH_FLD_PTR(roles,ingredientRoleList)
  YH_FLD_PTR(makeAtt,makeAtt)
  YH_FLD_PTR(initBlocks,methodList)
  YH_FLD_PTR(primeInitBlocks,methodList)
  YH_FLD_PTR(behavior, presenceBehavior)
  YH_FLD_PTR(unumImpl, unumImpl)
  YH_FLD_PTR(implements,implementsAttList)
  YH_FLD_PTR(facetInitBlocks,methodList)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(bindingType)
  YH_ARG_FIELD(attributes)
  YH_ARG_FIELD(structure)
  YH_ARG_FIELD(roles)
  YH_ARG_FIELD(makeAtt)
  YH_ARG_FIELD(initBlocks)
  YH_ARG_FIELD(primeInitBlocks)
  YH_ARG_FIELD(behavior)
  YH_ARG_FIELD(unumImpl)
  YH_ARG_FIELD(implements)
  YH_ARG_FIELD(facetInitBlocks)
)

YH_DEF_TAGGED(presenceImplAtt,
  YH_FLD_PTR(names,symbolList)
  YH_FLD_PTR(presence,presenceImplRef)
  ,
  YH_ARG_FIELD(names)
  YH_ARG_FIELD(presence)
)

YH_DEF_TAGGED(presenceImplDef,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_PTR(elems,elemList)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(elems)
)

YH_DEF_TAGGED(presenceImplRef,
  YH_FLD_PTR(name,symbolRef)
  ,
  YH_ARG_FIELD(name)
)

YH_DEF_TAGGED(presenceRole,
  YH_FLD_PTR(presences,symbolList)
  YH_FLD_PTR(impl,presenceImpl)
  ,
  YH_ARG_FIELD(presences)
  YH_ARG_FIELD(impl)
)
YH_LIST_DEF(presenceRole)

YH_DEF_TAGGED(presenceStructure, 
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_LONG(bindingType)
  YH_FLD_PTR(attributes,attributeList)
  YH_FLD_PTR(kind,kind)
  YH_FLD_PTR(ingredients,ingredientList)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(bindingType)
  YH_ARG_FIELD(attributes)
  YH_ARG_FIELD(kind)
  YH_ARG_FIELD(ingredients)
)

YH_DEF_TAGGED(presenceStructureDef,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_PTR(elems,elemList)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(elems)
)

YH_DEF_TAGGED(presenceStructureRef,
  YH_FLD_PTR(name,symbolRef)
  ,
  YH_ARG_FIELD(name)
)

YH_DEF_TAGGED(primeAtt,
  YH_FLD_PTR(name,symbol)
  ,
  YH_ARG_FIELD(name)
)

YH_DEF_TAGGED(primType,
  YH_FLD_LONG(type)
  ,
  YH_ARG_FIELD(type)
)

YH_DEF_TAGGED(protoDef,
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(params,parameterDeclList)
  YH_FLD_PTR(throws,scopedRefList)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(params)
  YH_ARG_FIELD(throws)
)
YH_LIST_DEF(protoDef)

YH_DEF_TAGGED(publishDef,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(name,symbol)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(name)
)

YH_DEF_TAGGED(refTerm,
  YH_FLD_PTR(value,symbolRef)
  ,
  YH_ARG_FIELD(value)
)

YH_DEF_TAGGED(remoteDef,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(url,string)
  YH_FLD_PTR(name,symbol)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(url)
  YH_ARG_FIELD(name)
)

YH_DEF_TAGGED(requireAtt,
  YH_FLD_PTR(expr,expr)
  YH_FLD_PTR(value,typedValue)
  ,
  YH_ARG_FIELD(expr)
  YH_ARG_FIELD(value)
)

YH_DEF_TAGGED(scope,
  YH_FLD_PTRN(outer,scope)
  YH_FLD_PTRN(imported,scopeList)
  YH_FLD_PTR(nameSpaces,nameSpaceList)
  YH_FLD_PTR(name,symbolDef)
  ,
  YH_ARG_FIELD(outer)
  YH_ARG_FIELD(imported)
  YH_ARG_FIELD(nameSpaces)
  YH_ARG_FIELD(name)
)
YH_LIST_DEF(scope)

YH_DEF_TAGGED(scopedRef,
  YH_FLD_PTR(scope,scopedRef)
  YH_FLD_PTR(ref,symbolRef)
  ,
  YH_ARG_FIELD(scope)
  YH_ARG_FIELD(ref)
)
YH_LIST_DEF(scopedRef)

YH_DEF_TAGGED(sequenceType,
  YH_FLD_PTR(type,type)
  YH_FLD_LONG(dimension)
  ,
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(dimension)
)

YH_DEF_TAGGED(sequenceTypeDecl,
  YH_FLD_PTR(type,typeSpec)
  YH_FLD_LONG(dimension)
  ,
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(dimension)
)

YH_DEF_TAGGED(simpleDeclarator,
  YH_FLD_PTR(name,symbol)
  ,
  YH_ARG_FIELD(name)
)

YH_DEF_TAGGED(stateBundle,
  YH_FLD_PTR(packagename,symbol)
  YH_FLD_PTR(typename,symbol)
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(init,string)
  ,
  YH_ARG_FIELD(packagename)
  YH_ARG_FIELD(typename)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(init)
)
YH_LIST_DEF(stateBundle)

YH_DEF_TAGGED(stateBundleDef,
  YH_FLD_PTR(typename,scopedRef)
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(init,string)
  ,
  YH_ARG_FIELD(typename)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(init)
)

YH_DEF_TAGGED(stringLit,
  YH_FLD_PTR(value,string)
  ,
  YH_ARG_FIELD(value)
)

YH_DEF_TAGGED(stringType,
  YH_FLD_LONG(dimension)
  ,
  YH_ARG_FIELD(dimension)
)

YH_DEF_TAGGED(structType,
  YH_FLD_PTR(members,memberList)
  ,
  YH_ARG_FIELD(members)
)

YH_DEF_TAGGED(structTypeDecl,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(members,memberDeclList)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(members)
)

YH_DEF_TAGGED(switchCase,
  YH_FLD_PTR(caseLabels,caseLabelList)
  YH_FLD_PTR(element,member)
  ,
  YH_ARG_FIELD(caseLabels)
  YH_ARG_FIELD(element)
)
YH_LIST_DEF(switchCase)

YH_DEF_TAGGED(switchCaseDecl,
  YH_FLD_PTR(caseLabels,caseLabelDeclList)
  YH_FLD_PTR(element,elementSpec)
  ,
  YH_ARG_FIELD(caseLabels)
  YH_ARG_FIELD(element)
)
YH_LIST_DEF(switchCaseDecl)

YH_DEF_TAGGED(symbolDef,
  YH_FLD_PTR(name,symbol)
  ,
  YH_ARG_FIELD(name)
)

YH_DEF_TAGGED(symbolRef,
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(params,exprList)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(params)
)

YH_DEF_TAGGED(tagLit,
  YH_FLD_BOOL(value)
  ,
  YH_ARG_FIELD(value)
)

YH_DEF_TAGGED(template,
  YH_FLD_LONG(bindingType)
  YH_FLD_PTR(ingredientImpl,ingredientImpl)
  YH_FLD_PTR(mapAtts,mapAttList)
  ,
  YH_ARG_FIELD(bindingType)
  YH_ARG_FIELD(ingredientImpl)
  YH_ARG_FIELD(mapAtts)
)

YH_DEF_TAGGED(templateAtt,
  YH_FLD_PTR(ingredients,symbolList)
  YH_FLD_PTR(template,templateDef)
  ,
  YH_ARG_FIELD(ingredients)
  YH_ARG_FIELD(template)
)

YH_DEF_TAGGED(templateDef,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(elems,elemList)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(elems)
)

YH_DEF_TAGGED(type,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)
YH_LIST_DEF(type)

YH_DEF_TAGGED(typeDeclarator,
  YH_FLD_PTR(type,typeSpec)
  YH_FLD_PTR(declarators,declaratorList)
  ,
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(declarators)
)

YH_DEF_TAGGED(typeDef,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(decl,typeDeclarator)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(decl)
)

YH_DEF_TAGGED(typedValue,
  YH_FLD_VAR(typeCode, value,
    YH_VAR_CASE(TV_UND, long, value)
    YH_VAR_CASE(TV_DEP, long, value)
    YH_VAR_CASE(TV_TAG, bool, value)
    YH_VAR_CASE(TV_BOOL, long, value)
    YH_VAR_CASE(TV_CHAR, char, value)
    YH_VAR_CASE(TV_LONG, long, value)
    YH_VAR_CASE(TV_FLOAT, float, value)
    YH_VAR_CASE(TV_STRING, charp, value)
    YH_VAR_CASE(TV_OTHER, charp, value)
  )
  ,
  YH_ARG_FIELD(typeCode)
  YH_ARG_FIELD(value)
)

YH_DEF_TAGGED(typeSpec,
  YH_FLD_LONG(dummy)
  ,
  YH_ARG_FIELD(dummy)
)

YH_DEF_TAGGED(undefinedType,
  YH_FLD_PTR(type,symbolRef)
  ,
  YH_ARG_FIELD(type)
)

YH_DEF_TAGGED(unionType,
  YH_FLD_PTR(switchType,type)
  YH_FLD_PTR(cases,switchCaseList)
  ,
  YH_ARG_FIELD(switchType)
  YH_ARG_FIELD(cases)
)

YH_DEF_TAGGED(unionTypeDecl,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(name,symbol)
  YH_FLD_PTR(switchType,typeSpec)
  YH_FLD_PTR(cases,switchCaseDeclList)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(switchType)
  YH_ARG_FIELD(cases)
)

YH_DEF_TAGGED(unit,
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_LONG(bindingType)
  YH_FLD_PTR(filePath,string)
  YH_FLD_PTR(scope,scope)
  YH_FLD_LONG(export)
  YH_FLD_BOOL(isImported)
  YH_FLD_PTR(imports,importAttList)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(bindingType)
  YH_ARG_FIELD(filePath)
  YH_ARG_FIELD(scope)
  YH_ARG_FIELD(export)
  YH_ARG_FIELD(isImported)
  YH_ARG_FIELD(imports)
)
YH_LIST_DEF(unit)

YH_DEF_TAGGED(unitDef,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_PTR(elems,elemList)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(elems)
)

YH_DEF_TAGGED(unitRef,
  YH_FLD_PTR(name,symbolRef)
  YH_FLD_LONG(export)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(export)
)

YH_DEF_TAGGED(unop,
  YH_FLD_LONG(op)
  YH_FLD_PTR(operand,expr)
  ,
  YH_ARG_FIELD(op)
  YH_ARG_FIELD(operand)
)

YH_DEF_TAGGED(unumImpl,
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_LONG(bindingType)
  YH_FLD_PTR(attributes,attributeList)
  YH_FLD_PTR(structure,unumStructure)
  YH_FLD_PTR(roles,presenceRoleList)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(bindingType)
  YH_ARG_FIELD(attributes)
  YH_ARG_FIELD(structure)
  YH_ARG_FIELD(roles)
)

YH_DEF_TAGGED(unumImplDef,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_PTR(elems,elemList)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(elems)
)

YH_DEF_TAGGED(unumImplRef,
  YH_FLD_PTR(name,symbolRef)
  ,
  YH_ARG_FIELD(name)
)

YH_DEF_TAGGED(unumStructure,
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_LONG(bindingType)
  YH_FLD_PTR(attributes,attributeList)
  YH_FLD_PTR(kind,kind)
  YH_FLD_PTR(presences,presenceList)
  YH_FLD_PTR(prime,symbol)
  ,
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(bindingType)
  YH_ARG_FIELD(attributes)
  YH_ARG_FIELD(kind)
  YH_ARG_FIELD(presences)
  YH_ARG_FIELD(prime)
)

YH_DEF_TAGGED(unumStructureDef,
  YH_FLD_PTR(info,info)
  YH_FLD_PTR(name,symbolDef)
  YH_FLD_PTR(elems,elemList)
  ,
  YH_ARG_FIELD(info)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(elems)
)

YH_DEF_TAGGED(unumStructureRef,
  YH_FLD_PTR(name,symbolRef)
  ,
  YH_ARG_FIELD(name)
)

YH_DEF_TAGGED(variable,
  YH_FLD_PTR(type,type)
  YH_FLD_LONG(modifiers)
  YH_FLD_PTR(name,symbol)
  YH_FLD_LONG(dimensions)
  YH_FLD_PTR(init,string)
  ,
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(dimensions)
  YH_ARG_FIELD(init)
)
YH_LIST_DEF(variable)

YH_DEF_TAGGED(variableDecl,
  YH_FLD_PTR(type,typeSpec)
  YH_FLD_LONG(modifiers)
  YH_FLD_PTR(name,symbol)
  YH_FLD_LONG(dimensions)
  YH_FLD_PTR(init,string)
  ,
  YH_ARG_FIELD(type)
  YH_ARG_FIELD(modifiers)
  YH_ARG_FIELD(name)
  YH_ARG_FIELD(dimensions)
  YH_ARG_FIELD(init)
)
YH_LIST_DEF(variableDecl)

YH_LIST_DEF(string)
YH_LIST_DEF(symbol)

