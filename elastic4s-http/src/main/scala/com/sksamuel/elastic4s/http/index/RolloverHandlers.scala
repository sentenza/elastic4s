package com.sksamuel.elastic4s.http.index

import com.fasterxml.jackson.annotation.JsonProperty
import com.sksamuel.elastic4s.admin.RolloverIndexRequest
import com.sksamuel.elastic4s.http._
import com.sksamuel.elastic4s.json.XContentFactory
import org.apache.http.entity.ContentType

case class RolloverResponse(@JsonProperty("old_index") oldIndex: String,
                            @JsonProperty("new_index") newIndex: String,
                            @JsonProperty("rolled_over") rolledOver: Boolean,
                            @JsonProperty("dry_run") dryRun: Boolean,
                            acknowledged: Boolean,
                            @JsonProperty("shards_acknowledged") shardsAcknowledged: Boolean,
                            conditions: Map[String, Boolean])

trait RolloverHandlers {

  implicit object RolloverHandler extends Handler[RolloverIndexRequest, RolloverResponse] {

    override def build(request: RolloverIndexRequest): ElasticRequest = {

      val endpoint  = s"/${request.sourceAlias}/_rollover"
      val endpoint2 = request.newIndexName.fold(endpoint)(endpoint + "/" + _)

      val params = scala.collection.mutable.Map.empty[String, Any]
      request.dryRun.foreach(params.put("dry_run", _))

      val builder = XContentFactory.jsonBuilder()
      builder.startObject("conditions")
      request.maxAge.foreach(builder.field("max_age", _))
      request.maxDocs.foreach(builder.field("max_docs", _))
      request.maxSize.foreach(builder.field("max_size", _))
      builder.endObject()

      val entity = HttpEntity(builder.string, ContentType.APPLICATION_JSON.getMimeType)
      ElasticRequest("POST", endpoint2, params.toMap, entity)
    }
  }
}
