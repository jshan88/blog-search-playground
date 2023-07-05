# API Documentation

URI : *http://localhost:8080*

HTTP request | Description
------------- | -------------
**GET** /blogs | 블로그 검색
**GET** /blogs/top-keywords | 인기 키워드 목록 조회 (Top 10, 내림차순)


## **GET** /blogs

### Parameters
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **query** | **String**| 검색어 |
 **sort** | **String**| 정렬 유형 | ACCURACY(정확도), RECENCY(최신)
 **page** | **int**| 페이지 번호 (1-50) | Optional, Default 1
 **size** | **int**| 페이지당 표출 문서 수 (1-50) | Optional, Default 10 

### Return
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**totalCount** | **int** | 총 항목 개수 | 
**totalPage** | **int** | 총 페이지 수 | 
**currentPage** | **int** | 현재 페이지 번호 | 
**documents** | [**List&lt;Document&gt;**](Document.md) | 검색 결과 항목 리스트 |

### Curl sample

```bash
curl -X GET "http://localhost:8080/blogs?query=카카오뱅크&sort=ACCURACY&page=1&size=10"
```
[[README 돌아가기]](../README.md#11-블로그-검색-기능#12-인기-검색어-목록-기능)

#
## **GET** /blogs/top-keywords
인기 키워드 목록 조회 (Top 10, 내림차순)

### Parameters
없음

### Return
[**List&lt;TopKeywordsResponse&gt;**](TopKeywordsResponse.md)

### Curl sample

```bash
curl -X GET "http://localhost:8080/blogs/top-keywords
```
[[README 돌아가기]](../README.md)
