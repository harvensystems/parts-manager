/* eslint-disable */
/* tslint:disable */
// @ts-nocheck
/*
 * ---------------------------------------------------------------
 * ## THIS FILE WAS GENERATED VIA SWAGGER-TYPESCRIPT-API        ##
 * ##                                                           ##
 * ## AUTHOR: acacode                                           ##
 * ## SOURCE: https://github.com/acacode/swagger-typescript-api ##
 * ---------------------------------------------------------------
 */

/** System application settings DTO */
export interface AppSettingDto {
  /**
   * Low stock threshold alert quantity
   * @format int32
   * @min 0
   * @example 5
   */
  lowStockThreshold?: number;
  /**
   * Image compression quality (1-100)
   * @format int32
   * @min 1
   * @max 100
   * @example 80
   */
  imageQuality?: number;
  /**
   * Default page size for parts catalog
   * @format int32
   * @min 5
   * @max 200
   * @example 20
   */
  defaultPageSize?: number;
  /**
   * Auto process AI tasks upon upload
   * @example true
   */
  autoProcessAi?: boolean;
  /**
   * Active AI recognition provider
   * @example "gemini"
   */
  aiProvider?: string;
  /**
   * Custom API key for AI provider
   * @example "sk-..."
   */
  customApiKey?: string;
  /**
   * Timestamp of last settings update
   * @format date-time
   */
  updatedAt?: string;
}

export interface CreateOrUpdatePartDto {
  name: string;
  type?: string;
  manufacturer?: string;
  partNumber?: string;
  partCode?: string;
  location?: string;
  packageType?: string;
  mounting?: string;
  /** @format int32 */
  quantity: number;
  description?: string;
  photoIds?: string[];
  metadata?: Record<string, string>;
}

export interface PartResponseDto {
  id?: string;
  name?: string;
  type?: string;
  manufacturer?: string;
  partNumber?: string;
  partCode?: string;
  location?: string;
  packageType?: string;
  mounting?: string;
  /** @format int32 */
  quantity?: number;
  description?: string;
  photoIds?: string[];
  metadata?: Record<string, string>;
  /** @format int32 */
  createdAt?: number;
  /** @format int32 */
  updatedAt?: number;
}

export interface RecognitionTaskResponseDto {
  id?: string;
  photoId?: string;
  originalFilename?: string;
  contentType?: string;
  status?: "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED";
  aiResult?: Record<string, object>;
  part?: PartResponseDto;
  rawText?: string;
  /** @format double */
  confidence?: number;
  errorMessage?: string;
  /** @format int64 */
  processingTimeMs?: number;
  /** @format int32 */
  createdAt?: number;
  /** @format int32 */
  completedAt?: number;
}

export interface DataUrlUploadDto {
  dataUrl?: string;
  filename?: string;
}

export interface ImageUploadResponseDto {
  id?: string;
  url?: string;
}

export interface AdjustQuantityDto {
  /** @format int32 */
  delta?: number;
}

export interface PagePartResponseDto {
  /** @format int64 */
  totalElements?: number;
  /** @format int32 */
  totalPages?: number;
  /** @format int32 */
  size?: number;
  content?: PartResponseDto[];
  /** @format int32 */
  number?: number;
  sort?: SortObject;
  first?: boolean;
  last?: boolean;
  /** @format int32 */
  numberOfElements?: number;
  pageable?: PageableObject;
  empty?: boolean;
}

export interface PageableObject {
  /** @format int64 */
  offset?: number;
  sort?: SortObject;
  /** @format int32 */
  pageSize?: number;
  /** @format int32 */
  pageNumber?: number;
  paged?: boolean;
  unpaged?: boolean;
}

export interface SortObject {
  empty?: boolean;
  sorted?: boolean;
  unsorted?: boolean;
}

export interface DictionaryResponseDto {
  manufacturers?: string[];
  packages?: string[];
  parameters?: string[];
  components?: string[];
  locations?: string[];
  nextPartCode?: string;
  enabledAI?: boolean;
}

import type {
  AxiosInstance,
  AxiosRequestConfig,
  HeadersDefaults,
  ResponseType,
} from "axios";
import axios from "axios";

export type QueryParamsType = Record<string | number, any>;

export interface FullRequestParams
  extends Omit<AxiosRequestConfig, "data" | "params" | "url" | "responseType"> {
  /** set parameter to `true` for call `securityWorker` for this request */
  secure?: boolean;
  /** request path */
  path: string;
  /** content type of request body */
  type?: ContentType;
  /** query params */
  query?: QueryParamsType;
  /** format of response (i.e. response.json() -> format: "json") */
  format?: ResponseType;
  /** request body */
  body?: unknown;
}

export type RequestParams = Omit<
  FullRequestParams,
  "body" | "method" | "query" | "path"
>;

export interface ApiConfig<SecurityDataType = unknown>
  extends Omit<AxiosRequestConfig, "data" | "cancelToken"> {
  securityWorker?: (
    securityData: SecurityDataType | null,
  ) => Promise<AxiosRequestConfig | void> | AxiosRequestConfig | void;
  secure?: boolean;
  format?: ResponseType;
}

export enum ContentType {
  Json = "application/json",
  JsonApi = "application/vnd.api+json",
  FormData = "multipart/form-data",
  UrlEncoded = "application/x-www-form-urlencoded",
  Text = "text/plain",
}

export class HttpClient<SecurityDataType = unknown> {
  public instance: AxiosInstance;
  private securityData: SecurityDataType | null = null;
  private securityWorker?: ApiConfig<SecurityDataType>["securityWorker"];
  private secure?: boolean;
  private format?: ResponseType;

  constructor({
    securityWorker,
    secure,
    format,
    ...axiosConfig
  }: ApiConfig<SecurityDataType> = {}) {
    this.instance = axios.create({
      ...axiosConfig,
      baseURL: axiosConfig.baseURL || "http://localhost:8080",
    });
    this.secure = secure;
    this.format = format;
    this.securityWorker = securityWorker;
  }

  public setSecurityData = (data: SecurityDataType | null) => {
    this.securityData = data;
  };

  protected mergeRequestParams(
    params1: AxiosRequestConfig,
    params2?: AxiosRequestConfig,
  ): AxiosRequestConfig {
    const method = params1.method || (params2 && params2.method);

    return {
      ...this.instance.defaults,
      ...params1,
      ...(params2 || {}),
      headers: {
        ...((method &&
          this.instance.defaults.headers[
            method.toLowerCase() as keyof HeadersDefaults
          ]) ||
          {}),
        ...(params1.headers || {}),
        ...((params2 && params2.headers) || {}),
      },
    };
  }

  protected stringifyFormItem(formItem: unknown) {
    if (typeof formItem === "object" && formItem !== null) {
      return JSON.stringify(formItem);
    } else {
      return `${formItem}`;
    }
  }

  protected createFormData(input: Record<string, unknown>): FormData {
    if (input instanceof FormData) {
      return input;
    }
    return Object.keys(input || {}).reduce((formData, key) => {
      const property = input[key];
      const propertyContent: any[] =
        property instanceof Array ? property : [property];

      for (const formItem of propertyContent) {
        const isFileType = formItem instanceof Blob || formItem instanceof File;
        formData.append(
          key,
          isFileType ? formItem : this.stringifyFormItem(formItem),
        );
      }

      return formData;
    }, new FormData());
  }

  public request = async <T = any, _E = any>({
    secure,
    path,
    type,
    query,
    format,
    body,
    ...params
  }: FullRequestParams): Promise<T> => {
    const secureParams =
      ((typeof secure === "boolean" ? secure : this.secure) &&
        this.securityWorker &&
        (await this.securityWorker(this.securityData))) ||
      {};
    const requestParams = this.mergeRequestParams(params, secureParams);
    const responseFormat = format || this.format || undefined;

    if (
      type === ContentType.FormData &&
      body &&
      body !== null &&
      typeof body === "object"
    ) {
      body = this.createFormData(body as Record<string, unknown>);
    }

    if (
      type === ContentType.Text &&
      body &&
      body !== null &&
      typeof body !== "string"
    ) {
      body = JSON.stringify(body);
    }

    return this.instance
      .request({
        ...requestParams,
        headers: {
          ...(requestParams.headers || {}),
          ...(type ? { "Content-Type": type } : {}),
        },
        params: query,
        responseType: responseFormat,
        data: body,
        url: path,
      })
      .then((response) => response.data);
  };
}

/**
 * @title Parts Manager API
 * @version v1.0
 * @license MIT (https://opensource.org/licenses/MIT)
 * @baseUrl http://localhost:8080
 *
 * REST API for AI-powered electronics workshop component and inventory management system.
 */
export class Api<
  SecurityDataType extends unknown,
> extends HttpClient<SecurityDataType> {
  settings = {
    /**
     * No description
     *
     * @tags Settings
     * @name GetSettings
     * @summary Get application settings
     * @request GET:/api/settings
     */
    getSettings: (params: RequestParams = {}) =>
      this.request<AppSettingDto, any>({
        path: `/api/settings`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Settings
     * @name UpdateSettings
     * @summary Update application settings
     * @request PUT:/api/settings
     */
    updateSettings: (data: AppSettingDto, params: RequestParams = {}) =>
      this.request<AppSettingDto, any>({
        path: `/api/settings`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),
  };
  parts = {
    /**
     * No description
     *
     * @tags Parts
     * @name GetPartById
     * @summary Get component by ID
     * @request GET:/api/parts/{id}
     */
    getPartById: (id: string, params: RequestParams = {}) =>
      this.request<PartResponseDto, any>({
        path: `/api/parts/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Parts
     * @name UpdatePart
     * @summary Update component by ID
     * @request PUT:/api/parts/{id}
     */
    updatePart: (
      id: string,
      data: CreateOrUpdatePartDto,
      params: RequestParams = {},
    ) =>
      this.request<PartResponseDto, any>({
        path: `/api/parts/${id}`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags Parts
     * @name DeletePart
     * @summary Delete component by ID
     * @request DELETE:/api/parts/{id}
     */
    deletePart: (id: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/parts/${id}`,
        method: "DELETE",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Parts
     * @name ListParts
     * @summary List components with optional search and filters
     * @request GET:/api/parts
     */
    listParts: (
      query?: {
        /** Search query (name, part number, package, metadata) */
        search?: string;
        /** Component type filter (e.g. Resistor, IC) */
        type?: string;
        /** Mounting filter (SMD or Through-hole) */
        mounting?: string;
        /**
         * Sort property (updatedAt, name, quantity)
         * @default "updatedAt"
         */
        sortBy?: string;
      },
      params: RequestParams = {},
    ) =>
      this.request<PartResponseDto[], any>({
        path: `/api/parts`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags Parts
     * @name SaveOrUpdatePart
     * @summary Create or update component (automatically increments quantity if Part Number matches)
     * @request POST:/api/parts
     */
    saveOrUpdatePart: (
      data: CreateOrUpdatePartDto,
      params: RequestParams = {},
    ) =>
      this.request<PartResponseDto, any>({
        path: `/api/parts`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags Parts
     * @name AdjustQuantity
     * @summary Adjust component stock quantity (increment or decrement)
     * @request PATCH:/api/parts/{id}/quantity
     */
    adjustQuantity: (
      id: string,
      data: AdjustQuantityDto,
      params: RequestParams = {},
    ) =>
      this.request<PartResponseDto, any>({
        path: `/api/parts/${id}/quantity`,
        method: "PATCH",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags Parts
     * @name PagedParts
     * @summary Get paginated components list
     * @request GET:/api/parts/paged
     */
    pagedParts: (
      query?: {
        /** Search query */
        search?: string;
        /** Component type filter */
        type?: string;
        /** Mounting filter */
        mounting?: string;
        /**
         * Page number (0-indexed)
         * @format int32
         * @default 0
         */
        page?: number;
        /**
         * Page size
         * @format int32
         * @default 20
         */
        size?: number;
        /**
         * Sort field
         * @default "updatedAt"
         */
        sortBy?: string;
        /**
         * Sort direction (asc, desc)
         * @default "desc"
         */
        direction?: string;
      },
      params: RequestParams = {},
    ) =>
      this.request<PagePartResponseDto, any>({
        path: `/api/parts/paged`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags Parts
     * @name GetDictionary
     * @summary Get list of components with optional search and filters
     * @request GET:/api/parts/dictionary
     */
    getDictionary: (params: RequestParams = {}) =>
      this.request<DictionaryResponseDto, any>({
        path: `/api/parts/dictionary`,
        method: "GET",
        ...params,
      }),
  };
  recognition = {
    /**
     * No description
     *
     * @tags Recognition
     * @name RetryTask
     * @summary Retry AI recognition for a task
     * @request POST:/api/recognition-tasks/{id}/retry
     */
    retryTask: (id: string, params: RequestParams = {}) =>
      this.request<RecognitionTaskResponseDto, any>({
        path: `/api/recognition-tasks/${id}/retry`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Recognition
     * @name UploadDataUrl
     * @summary Upload Base64/DataURL image (e.g. from camera capture) for AI recognition
     * @request POST:/api/recognition-tasks/data-url
     */
    uploadDataUrl: (data: DataUrlUploadDto, params: RequestParams = {}) =>
      this.request<RecognitionTaskResponseDto, any>({
        path: `/api/recognition-tasks/data-url`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags Recognition
     * @name ListTasks
     * @summary Get all recognition tasks (queue)
     * @request GET:/api/recognition-tasks
     */
    listTasks: (params: RequestParams = {}) =>
      this.request<RecognitionTaskResponseDto[], any>({
        path: `/api/recognition-tasks`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Recognition
     * @name UploadFile
     * @summary Upload image file as multipart/form-data for AI recognition
     * @request POST:/api/recognition-tasks
     */
    uploadFile: (
      data: {
        /**
         * Image file (JPEG/PNG/WEBP)
         * @format binary
         */
        file: File;
      },
      params: RequestParams = {},
    ) =>
      this.request<RecognitionTaskResponseDto, any>({
        path: `/api/recognition-tasks`,
        method: "POST",
        body: data,
        type: ContentType.FormData,
        ...params,
      }),

    /**
     * No description
     *
     * @tags Recognition
     * @name GetTask
     * @summary Get recognition task status by ID
     * @request GET:/api/recognition-tasks/{id}
     */
    getTask: (id: string, params: RequestParams = {}) =>
      this.request<RecognitionTaskResponseDto, any>({
        path: `/api/recognition-tasks/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Recognition
     * @name DeleteTask
     * @summary Delete recognition task and its associated image
     * @request DELETE:/api/recognition-tasks/{id}
     */
    deleteTask: (id: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/recognition-tasks/${id}`,
        method: "DELETE",
        ...params,
      }),
  };
  images = {
    /**
     * No description
     *
     * @tags Images
     * @name UploadImage
     * @summary Upload image into GridFS
     * @request POST:/api/images
     */
    uploadImage: (
      data: {
        /**
         * Image file to upload
         * @format binary
         */
        file: File;
      },
      params: RequestParams = {},
    ) =>
      this.request<ImageUploadResponseDto, any>({
        path: `/api/images`,
        method: "POST",
        body: data,
        type: ContentType.FormData,
        ...params,
      }),

    /**
     * No description
     *
     * @tags Images
     * @name GetImage
     * @summary Get binary image by ID
     * @request GET:/api/images/{id}
     */
    getImage: (id: string, params: RequestParams = {}) =>
      this.request<Blob, any>({
        path: `/api/images/${id}`,
        method: "GET",
        format: "blob",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Images
     * @name DeleteImage
     * @summary Delete image by ID
     * @request DELETE:/api/images/{id}
     */
    deleteImage: (id: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/images/${id}`,
        method: "DELETE",
        ...params,
      }),
  };
}
