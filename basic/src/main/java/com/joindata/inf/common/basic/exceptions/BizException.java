package com.joindata.inf.common.basic.exceptions;

import com.joindata.inf.common.basic.entities.ErrorEntity;

public class BizException extends RuntimeException
{
    private static final long serialVersionUID = 8564997768294356109L;

    public ErrorEntity getErrorEntity()
    {
        return ErrorEntity.define(code, message);
    }

    private int code;

    private String message;

    public BizException(int code, String message)
    {
        this.code = code;
        this.message = message;
    }

    public BizException(ErrorEntity entity)
    {
        this.code = entity.getCode();
        this.message = entity.getMessage();
    }

    public BizException(String message)
    {
        super();
        this.message = message;
    }

    public BizException()
    {
    }

    @Override
    public String getMessage()
    {
        return this.message;
    }

}
