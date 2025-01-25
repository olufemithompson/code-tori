import { Body, Controller, Get, Post } from '@nestjs/common';
import { AppService } from './app.service';
import { ExplainFunctionResponse } from './explain-function.response.dto';
import { ExplainFunctionRequest } from './explain-function.request.dto';
import e from 'express';

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Post('explain')
  async explain(
    @Body() explainFunctionRequest: ExplainFunctionRequest,
  ): Promise<ExplainFunctionResponse> {
    return this.appService.explainFunction(explainFunctionRequest);
  }
}
