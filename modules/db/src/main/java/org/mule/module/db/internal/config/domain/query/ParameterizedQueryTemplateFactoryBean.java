/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.db.internal.config.domain.query;

import static org.apache.commons.collections.CollectionUtils.find;
import org.mule.module.db.internal.domain.param.InputQueryParam;
import org.mule.module.db.internal.domain.param.QueryParam;
import org.mule.module.db.internal.domain.query.QueryTemplate;
import org.mule.module.db.internal.parser.QueryTemplateParser;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.Predicate;
import org.springframework.beans.factory.FactoryBean;

/**
 * Creates {@link QueryTemplate} for parameterized queries
 */
public class ParameterizedQueryTemplateFactoryBean implements FactoryBean<QueryTemplate>
{

    private final String sqlText;
    private final List<QueryParam> queryParams;
    private final QueryTemplateParser queryParser;

    public ParameterizedQueryTemplateFactoryBean(String sqlText, List<QueryParam> queryParams, QueryTemplateParser queryParser)
    {
        this.sqlText = sqlText;
        this.queryParams = queryParams;
        this.queryParser = queryParser;
    }

    @Override
    public QueryTemplate getObject() throws Exception
    {
        QueryTemplate queryTemplate = queryParser.parse(sqlText);

        List<QueryParam> resolvedParams = new LinkedList<QueryParam>();

        for (InputQueryParam inputSqlParam : queryTemplate.getInputParams())
        {
            QueryParam param = findOverriddenParam(inputSqlParam.getName(), queryParams);

            if (param == null)
            {
                resolvedParams.add(inputSqlParam);
            }
            else
            {
                resolvedParams.add(param);
            }
        }

        return new QueryTemplate(queryTemplate.getSqlText(), queryTemplate.getType(), resolvedParams);
    }

    private QueryParam findOverriddenParam(final String name, List<QueryParam> queryParams)
    {
        if (name != null)
        {
            return (QueryParam) find(queryParams, new Predicate()
            {
                @Override
                public boolean evaluate(Object object)
                {
                    return name.equals(((QueryParam) object).getName());
                }
            });
        }

        return null;
    }

    @Override
    public Class<?> getObjectType()
    {
        return QueryTemplate.class;
    }

    @Override
    public boolean isSingleton()
    {
        return false;
    }
}
